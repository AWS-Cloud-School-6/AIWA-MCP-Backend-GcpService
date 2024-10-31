package AIWA.McpBackend.service.aws;


import AIWA.McpBackend.controller.api.dto.ec2.Ec2InstanceDTO;
import AIWA.McpBackend.controller.api.dto.eip.EipDto;
import AIWA.McpBackend.controller.api.dto.eni.NetworkInterfaceDto;
import AIWA.McpBackend.controller.api.dto.internetgateway.InternetGatewayDto;
import AIWA.McpBackend.controller.api.dto.natgateway.NatGatewayDto;
import AIWA.McpBackend.controller.api.dto.response.SingleResult;
import AIWA.McpBackend.controller.api.dto.routetable.RouteDTO;
import AIWA.McpBackend.controller.api.dto.routetable.RouteTableResponseDto;
import AIWA.McpBackend.controller.api.dto.securitygroup.SecurityGroupDTO;
import AIWA.McpBackend.controller.api.dto.subnet.SubnetResponseDto;
import AIWA.McpBackend.controller.api.dto.vpc.VpcTotalResponseDto;
import AIWA.McpBackend.provider.aws.api.dto.member.MemberCredentialDTO;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AwsResourceService {

    private final RestTemplate restTemplate;

    private Ec2Client ec2Client;

    public void initializeClient(String email) {
        // 특정 멤버의 AWS 자격 증명 가져오기
        MemberCredentialDTO memberCredentialDto = getMemberCredentials(email);

        if (memberCredentialDto == null) {
            throw new IllegalArgumentException("회원 정보를 찾을 수 없습니다.");
        }

        // AWS 자격 증명 생성
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(
                memberCredentialDto.getAccessKey(),
                memberCredentialDto.getSecretKey()
        );

        // EC2 클라이언트 생성
        this.ec2Client = Ec2Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .region(Region.of("ap-northeast-2")) // Member에서 리전 가져오기
                .build();
    }

    private MemberCredentialDTO getMemberCredentials(String email) {
        String url = "http://" + "member-svc" + "/member/api/members/email?email=" + email;

        try {
            // SingleResult로 응답을 받음
            ResponseEntity<SingleResult<MemberCredentialDTO>> response =
                    restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<SingleResult<MemberCredentialDTO>>() {});

            // 응답 상태 코드와 데이터 유효성 확인
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().isSuccess()) {
                return response.getBody().getData(); // SingleResult에서 MemberCredentialDTO 추출
            } else {
                return null;
            }
        } catch (Exception e) {
            // 오류 처리
            e.printStackTrace();
            return null;
        }
    }

    // EC2 Instances 가져오기
    public List<Ec2InstanceDTO> fetchEc2Instances(String userId) {
        initializeClient(userId);
        DescribeInstancesRequest request = DescribeInstancesRequest.builder().build();
        DescribeInstancesResponse response = ec2Client.describeInstances(request);
        List<Ec2InstanceDTO> ec2Instances = new ArrayList<>();

        response.reservations().forEach(reservation -> {
            reservation.instances().forEach(instance -> {
                String instanceState = instance.state().nameAsString();
                Map<String, String> tagsMap = instance.tags() == null ? Collections.emptyMap() :
                        instance.tags().stream().collect(Collectors.toMap(Tag::key, Tag::value));
                ec2Instances.add(new Ec2InstanceDTO(instance.instanceId(), instanceState, tagsMap));
            });
        });

        return ec2Instances;
    }

    // Subnets 가져오기
    public List<SubnetResponseDto> fetchSubnets(String userId) {

        initializeClient(userId);
        DescribeSubnetsRequest request = DescribeSubnetsRequest.builder().build();
        DescribeSubnetsResponse response = ec2Client.describeSubnets(request);
        return response.subnets().stream()
                .map(subnet -> {
                    Map<String, String> tagsMap = subnet.tags() == null ? Collections.emptyMap() :
                            subnet.tags().stream().collect(Collectors.toMap(Tag::key, Tag::value));
                    return new SubnetResponseDto(subnet.subnetId(), subnet.cidrBlock(), subnet.vpcId(), tagsMap, subnet.availabilityZone());
                })
                .collect(Collectors.toList());
    }

    // Route Tables 가져오기
    public List<RouteTableResponseDto> fetchRouteTables(String userId) {
        initializeClient(userId);
        DescribeRouteTablesRequest request = DescribeRouteTablesRequest.builder().build();
        DescribeRouteTablesResponse response = ec2Client.describeRouteTables(request);
        return response.routeTables().stream()
                .map(routeTable -> {
                    Map<String, String> tagsMap = routeTable.tags() == null ? Collections.emptyMap() :
                            routeTable.tags().stream().collect(Collectors.toMap(Tag::key, Tag::value));
                    List<RouteDTO> routes = routeTable.routes().stream()
                            .map(route -> new RouteDTO(route.gatewayId(), route.destinationCidrBlock()))
                            .collect(Collectors.toList());
                    return new RouteTableResponseDto(routeTable.routeTableId(), routeTable.vpcId(), routes, tagsMap);
                })
                .collect(Collectors.toList());
    }

    // VPCs 가져오기
    public List<VpcTotalResponseDto> fetchVpcs(String userId) {

        initializeClient(userId);

        DescribeVpcsRequest request = DescribeVpcsRequest.builder().build();
        DescribeVpcsResponse response = ec2Client.describeVpcs(request);

        List<SubnetResponseDto> subnets = fetchSubnets(userId);
        List<RouteTableResponseDto> routeTables = fetchRouteTables(userId);

        return response.vpcs().stream()
                .map(vpc -> {
                    Map<String, String> tagsMap = vpc.tags() == null ? Collections.emptyMap() :
                            vpc.tags().stream().collect(Collectors.toMap(Tag::key, Tag::value));

                    List<SubnetResponseDto> associatedSubnets = subnets.stream()
                            .filter(subnet -> subnet.getVpcId().equals(vpc.vpcId()))
                            .collect(Collectors.toList());

                    List<RouteTableResponseDto> associatedRouteTables = routeTables.stream()
                            .filter(routeTable -> routeTable.getVpcId().equals(vpc.vpcId()))
                            .collect(Collectors.toList());

                    return new VpcTotalResponseDto(vpc.vpcId(), vpc.cidrBlock(), tagsMap, associatedSubnets, associatedRouteTables);
                })
                .collect(Collectors.toList());
    }

    // Security Groups 가져오기
    public List<SecurityGroupDTO> fetchSecurityGroups(String userId) {

        initializeClient(userId);
        DescribeSecurityGroupsRequest request = DescribeSecurityGroupsRequest.builder().build();
        DescribeSecurityGroupsResponse response = ec2Client.describeSecurityGroups(request);
        return response.securityGroups().stream()
                .map(group -> {
                    Map<String, String> tagsMap = group.tags() == null ? Collections.emptyMap() :
                            group.tags().stream().collect(Collectors.toMap(Tag::key, Tag::value));
                    return new SecurityGroupDTO(group.groupId(), group.groupName(), tagsMap);
                })
                .collect(Collectors.toList());
    }


    public List<InternetGatewayDto> fetchInternetGateways(String userId) {

        initializeClient(userId);
        DescribeInternetGatewaysRequest request = DescribeInternetGatewaysRequest.builder().build();
        DescribeInternetGatewaysResponse response = ec2Client.describeInternetGateways(request);

        return response.internetGateways().stream()
                .map(internetGateway -> {
                    // 태그를 맵으로 변환
                    Map<String, String> tagsMap = internetGateway.tags() == null ? Collections.emptyMap() :
                            internetGateway.tags().stream().collect(Collectors.toMap(Tag::key, Tag::value));

                    // Attachments의 정보를 적절하게 변환
                    List<InternetGatewayDto.Attachment> attachments = internetGateway.attachments().stream()
                            .map(attachment -> new InternetGatewayDto.Attachment(attachment.vpcId(), attachment.stateAsString())) // 상태를 문자열로 변환
                            .collect(Collectors.toList());

                    return new InternetGatewayDto(internetGateway.internetGatewayId(), tagsMap, attachments);
                })
                .collect(Collectors.toList());
    }

    // NAT Gateways 가져오기
    public List<NatGatewayDto> fetchNatGateways(String userId) {
        initializeClient(userId);
        DescribeNatGatewaysRequest request = DescribeNatGatewaysRequest.builder().build();
        DescribeNatGatewaysResponse response = ec2Client.describeNatGateways(request);

        // ENI 정보를 가져옵니다.
        List<NetworkInterfaceDto> networkInterfaces = fetchNetworkInterfaces(userId);
        Map<String, List<NetworkInterfaceDto>> natGatewayEniMap = new HashMap<>();

        // NAT Gateway와 연결된 ENI 정보를 매핑합니다.
        for (NetworkInterfaceDto eni : networkInterfaces) {
            for (var natGateway : response.natGateways()) {
                for (var address : natGateway.natGatewayAddresses()) {
                    if (address.networkInterfaceId() != null && address.networkInterfaceId().equals(eni.getNetworkInterfaceId())) {
                        natGatewayEniMap
                                .computeIfAbsent(natGateway.natGatewayId(), k -> new ArrayList<>())
                                .add(eni);
                    }
                }
            }
        }

        return response.natGateways().stream()
                .map(natGateway -> {
                    Map<String, String> tagsMap = natGateway.tags() == null ? Collections.emptyMap() :
                            natGateway.tags().stream().collect(Collectors.toMap(Tag::key, Tag::value));

                    // ENI ID 리스트 추출
                    List<String> networkInterfaceIds = natGateway.natGatewayAddresses().stream()
                            .map(address -> address.networkInterfaceId())
                            .collect(Collectors.toList());

                    // ENI DTO 리스트 가져오기
                    List<NetworkInterfaceDto> eniList = natGatewayEniMap.getOrDefault(natGateway.natGatewayId(), Collections.emptyList());

                    // NatGatewayDto 생성
                    return new NatGatewayDto(
                            natGateway.natGatewayId(),
                            natGateway.stateAsString(),
                            tagsMap,
                            natGateway.vpcId(),
                            eniList // 연결된 ENI DTO 리스트
                    );
                })
                .collect(Collectors.toList());
    }


    public List<EipDto> fetchElasticIps(String userId) {
        initializeClient(userId);
        DescribeAddressesRequest request = DescribeAddressesRequest.builder().build();
        DescribeAddressesResponse response = ec2Client.describeAddresses(request);

        return response.addresses().stream()
                .map(address -> {
                    Map<String, String> tagsMap = address.tags() == null ? Collections.emptyMap() :
                            address.tags().stream().collect(Collectors.toMap(Tag::key, Tag::value));

                    return new EipDto(address.allocationId(), address.publicIp(), address.domain().name(), tagsMap);
                })
                .collect(Collectors.toList());
    }



    public List<NetworkInterfaceDto> fetchNetworkInterfaces(String userId) {
        initializeClient(userId);
        DescribeNetworkInterfacesRequest request = DescribeNetworkInterfacesRequest.builder().build();
        DescribeNetworkInterfacesResponse response = ec2Client.describeNetworkInterfaces(request);

        return response.networkInterfaces().stream()
                .map(networkInterface -> {
                    // 태그를 맵으로 변환
                    Map<String, String> tagsMap = (networkInterface.tagSet() == null || networkInterface.tagSet().isEmpty())
                            ? Collections.emptyMap()
                            : networkInterface.tagSet().stream().collect(Collectors.toMap(Tag::key, Tag::value));

                    List<String> privateIpAddresses = networkInterface.privateIpAddresses().stream()
                            .map(ipAddress -> ipAddress.privateIpAddress())
                            .collect(Collectors.toList());

                    // Public IP 리스트 초기화
                    List<String> publicIpAddresses = (networkInterface.association() != null && networkInterface.association().publicIp() != null)
                            ? List.of(networkInterface.association().publicIp())
                            : Collections.emptyList(); // Public IP가 없는 경우 빈 리스트

                    return new NetworkInterfaceDto(
                            networkInterface.networkInterfaceId(),
                            networkInterface.subnetId(),
                            networkInterface.vpcId(),
                            networkInterface.statusAsString(),
                            networkInterface.description(),
                            tagsMap,
                            privateIpAddresses,
                            publicIpAddresses // Public IP 추가
                    );
                })
                .collect(Collectors.toList());
    }


}
