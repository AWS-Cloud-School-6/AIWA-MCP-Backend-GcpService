package AIWA.McpBackend.service.gcp;

import AIWA.McpBackend.controller.api.dto.response.ListResult;
import AIWA.McpBackend.controller.api.dto.response.SingleResult;
import AIWA.McpBackend.controller.api.dto.staticip.StaticIpDto;
import AIWA.McpBackend.controller.api.dto.subnet.SubnetResponseDto;
import AIWA.McpBackend.controller.api.dto.vm.VmResponseDto;
import AIWA.McpBackend.controller.api.dto.vpc.VpcTotalResponseDto;
import AIWA.McpBackend.service.response.ResponseService;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.cloud.compute.v1.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class GcpResourceService {

    @Autowired
    private ResponseService responseService;
    private final RestTemplate restTemplate;
//    private final String credentialsPath = "C:\\Users\\USER\\Desktop\\AWS 수업자료\\프로젝트\\eighth-service-439605-r6-94e2daa99a67.json"; // 로컬 JSON 키 파일 경로

//    public void initializeClient(String email) {
//        // 특정 멤버의 AWS 자격 증명 가져오기
//        MemberCredentialDTO memberCredentialDto = getMemberCredentials(email);
//
//        if (memberCredentialDto == null) {
//            throw new IllegalArgumentException("회원 정보를 찾을 수 없습니다.");
//        }
//
//        // AWS 자격 증명 생성
//        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(
//                memberCredentialDto.getAccessKey(),
//                memberCredentialDto.getSecretKey()
//        );
//
//        // EC2 클라이언트 생성
//        this.ec2Client = Ec2Client.builder()
//                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
//                .region(Region.of("ap-northeast-2")) // Member에서 리전 가져오기
//                .build();
//    }
//
//    private MemberCredentialDTO getMemberCredentials(String email) {
//        String url = "http://" + "member-svc" + "/member/api/members/email?email=" + email;
//
//        try {
//            // SingleResult로 응답을 받음
//            ResponseEntity<SingleResult<MemberCredentialDTO>> response =
//                    restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<SingleResult<MemberCredentialDTO>>() {});
//
//            // 응답 상태 코드와 데이터 유효성 확인
//            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().isSuccess()) {
//                return response.getBody().getData(); // SingleResult에서 MemberCredentialDTO 추출
//            } else {
//                return null;
//            }
//        } catch (Exception e) {
//            // 오류 처리
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    // EC2 Instances 가져오기
//    public List<Ec2InstanceDTO> fetchEc2Instances(String userId) {
//        initializeClient(userId);
//        DescribeInstancesRequest request = DescribeInstancesRequest.builder().build();
//        DescribeInstancesResponse response = ec2Client.describeInstances(request);
//        List<Ec2InstanceDTO> ec2Instances = new ArrayList<>();
//
//        response.reservations().forEach(reservation -> {
//            reservation.instances().forEach(instance -> {
//                String instanceState = instance.state().nameAsString();
//                Map<String, String> tagsMap = instance.tags() == null ? Collections.emptyMap() :
//                        instance.tags().stream().collect(Collectors.toMap(Tag::key, Tag::value));
//                ec2Instances.add(new Ec2InstanceDTO(instance.instanceId(), instanceState, tagsMap));
//            });
//        });
//
//        return ec2Instances;
//    }

    private GoogleCredentials getCredentials() throws IOException {
        // JSON 키 파일 경로 설정
        String credentialsPath = "C:\\Users\\USER\\auth.json";
        return GoogleCredentials.fromStream(new FileInputStream(credentialsPath))
                .createScoped("https://www.googleapis.com/auth/cloud-platform");
    }

    //vm 조회
    public ResponseEntity<?> listInstances(String projectId) {
        List<VmResponseDto> instanceList = new ArrayList<>();

        try {
            // JSON 키 파일 경로 설정
            GoogleCredentials credentials = getCredentials();

            // InstancesClient 생성
            try (InstancesClient instancesClient = InstancesClient.create(InstancesSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build())) {

                // 모든 Zone에서 인스턴스를 검색
                AggregatedListInstancesRequest request = AggregatedListInstancesRequest.newBuilder()
                        .setProject(projectId)
                        .build();

                InstancesClient.AggregatedListPagedResponse response = instancesClient.aggregatedList(request);

                // `response`의 각 항목을 순회하면서 인스턴스 리스트 추출
                response.iterateAll().forEach((zoneScopedInstances) -> {
                    InstancesScopedList scopedList = zoneScopedInstances.getValue();

                    if (scopedList != null && scopedList.getInstancesList() != null) {
                        for (Instance instance : scopedList.getInstancesList()) {
                            String name = instance.getName();
                            String status = instance.getStatus();
                            String zone = zoneScopedInstances.getKey().substring(zoneScopedInstances.getKey().lastIndexOf('/') + 1);
                            String externalIp = instance.getNetworkInterfaces(0).getAccessConfigsList().isEmpty() ?
                                    null : instance.getNetworkInterfaces(0).getAccessConfigs(0).getNatIP();
                            String internalIp = instance.getNetworkInterfaces(0).getNetworkIP();

                            // VmResponseDto 객체에 인스턴스 정보 추가
                            instanceList.add(new VmResponseDto(name, status, zone, externalIp, internalIp));
                        }
                    }
                });

                // 인스턴스가 없는 경우 빈 목록을 반환
                return ResponseEntity.ok(responseService.getListResult(instanceList));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(responseService.getFailResult());
        }
    }

    //subnet 조회
    public ListResult<SubnetResponseDto> listSubnets(String projectId) throws IOException {
        // GCP 인증 처리 (서비스 계정 키 파일을 사용하여 인증)
        GoogleCredentials credentials = getCredentials();

        // 인증 정보를 SubnetworksSettings에 설정하여 SubnetworksClient 생성
        SubnetworksSettings subnetworksSettings = SubnetworksSettings.newBuilder()
                .setCredentialsProvider(() -> credentials)
                .build();

        // SubnetworksClient 객체 생성 (인증 정보와 함께 생성)
        try (SubnetworksClient subnetworksClient = SubnetworksClient.create(subnetworksSettings)) {

            String region = "us-central1";
            // ListSubnetworksRequest 객체 생성
            ListSubnetworksRequest listRequest = ListSubnetworksRequest.newBuilder()
                    .setProject(projectId)  // GCP 프로젝트 ID
                    .setRegion(region)  // GCP 리전
                    .build();

            // 서브넷 리스트 조회
            Iterable<Subnetwork> subnetworkIterable = subnetworksClient.list(listRequest).iterateAll();

            // 서브넷 항목을 DTO로 변환
            List<SubnetResponseDto> subnetDTOList = StreamSupport.stream(subnetworkIterable.spliterator(), false)
                    .map(subnetwork -> {
                        // 네트워크 ID와 CIDR 범위 등 기본 정보
                        String networkId = subnetwork.getNetwork();  // 네트워크 정보
                        String ipCidrRange = subnetwork.getIpCidrRange();  // CIDR 범위

                        // 레이블(태그) 정보를 List<String>으로 변환 (기본적으로 "default" 값으로 설정)
                        List<String> tags = Collections.singletonList("default"); // "default" 태그를 리스트로 감쌈

                        return new SubnetResponseDto(
                                String.valueOf(subnetwork.getId()),  // getId()를 String으로 변환
                                ipCidrRange,
                                networkId.split("/")[9],
                                tags,  // 태그 정보를 List로 처리
                                subnetwork.getRegion().split("/")[8]);
                    })
                    .collect(Collectors.toList());

            // 결과 모델에 데이터 세팅
            return responseService.getListResult(subnetDTOList);
        }
    }

    // VPC 정보와 서브넷 및 라우팅 테이블 조회
    public ResponseEntity<?> listVpcsWithDetails(String projectId) {
        List<VpcTotalResponseDto> vpcList = new ArrayList<>();

        try {
            // 인증 처리
            GoogleCredentials credentials = getCredentials();

            // NetworksClient 생성
            try (NetworksClient networksClient = NetworksClient.create(NetworksSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build())) {

                // VPC 네트워크 목록 요청
                ListNetworksRequest listNetworksRequest = ListNetworksRequest.newBuilder()
                        .setProject(projectId)  // GCP 프로젝트 ID
                        .build();

                // VPC 네트워크 목록을 페이징 처리하면서 조회
                NetworksClient.ListPagedResponse response = networksClient.list(listNetworksRequest);

                // 페이징된 네트워크 목록을 순회
                for (Network network : response.iterateAll()) {
                    // VPC ID, CIDR, Tags 등 추출
                    String vpcId = String.valueOf(network.getId());  // Network 객체에서 ID는 long 타입일 수 있으므로 문자열로 변환
                    String cidr = "Not Available"; // CIDR 범위 정보는 서브넷에서 가져와야 함
                    List<String> tags = new ArrayList<>(); // 태그를 가져올 방법을 변경해야 함

                    // 서브넷 정보 조회
                    List<String> subnets = new ArrayList<>();
                    List<String> cidrBlocks = new ArrayList<>();

                    // SubnetworksClient를 사용하여 서브넷 목록 조회
                    try (SubnetworksClient subnetworksClient = SubnetworksClient.create(SubnetworksSettings.newBuilder()
                            .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                            .build())) {

                        // 서브넷 목록 조회
                        String region = "us-central1"; // 필요한 지역을 지정
                        SubnetworksClient.ListPagedResponse subnetworkResponse = subnetworksClient.list(
                                projectId, region);

                        for (Subnetwork subnetwork : subnetworkResponse.iterateAll()) {
                            subnets.add(subnetwork.getName());
                            cidrBlocks.add(subnetwork.getIpCidrRange());
                        }

                        if (!cidrBlocks.isEmpty()) {
                            cidr = String.join(", ", cidrBlocks);
                        }
                    }

                    // 라우팅 테이블 조회 (RoutesClient 사용)
                    List<String> routingTables = new ArrayList<>();

                    // RoutesClient를 사용하여 라우팅 테이블 조회
                    try (RoutesClient routesClient = RoutesClient.create(RoutesSettings.newBuilder()
                            .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                            .build())) {

                        RoutesClient.ListPagedResponse routeResponse = routesClient.list(projectId);

                        // 라우트 리스트를 순회하며, 네트워크와 일치하는 라우트를 추출
                        for (Route route : routeResponse.iterateAll()) {
                            if (route.getNetwork().contains(network.getSelfLink())) { // 네트워크 URL과 일치하는 라우트를 찾음
                                routingTables.add(route.getName());
                            }
                        }
                    }

                    // VPC 정보 DTO 생성
                    VpcTotalResponseDto vpcDto = new VpcTotalResponseDto(vpcId, cidr, tags, subnets, routingTables);
                    vpcList.add(vpcDto);
                }

                // VPC가 없을 경우 빈 리스트 반환
                return ResponseEntity.ok(responseService.getListResult(vpcList));

            }
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(responseService.getFailResult());
        }
    }

    //static ip 조회
    public List<StaticIpDto> getStaticIpsFromGCP(String projectId) {
        String region = "us-central1";

        try {
            GoogleCredentials credentials = getCredentials();

            try (AddressesClient addressesClient = AddressesClient.create(AddressesSettings.newBuilder()
                    .setCredentialsProvider(() -> credentials)
                    .build())) {

                AggregatedListAddressesRequest request = AggregatedListAddressesRequest.newBuilder()
                        .setProject(projectId)
                        .build();

                List<StaticIpDto> staticIps = new ArrayList<>();
                AddressesClient.AggregatedListPagedResponse response = addressesClient.aggregatedList(request);

                for (Map.Entry<String, AddressesScopedList> entry : response.iterateAll()) {
                    AddressesScopedList scopedList = entry.getValue();
                    for (Address address : scopedList.getAddressesList()) {
                        if ("RESERVED".equals(address.getStatus()) || "IN_USE".equals(address.getStatus())) {
                            StaticIpDto staticIpInfoDTO = new StaticIpDto(
                                    address.getName(),
                                    address.getAddress(),
                                    address.getAddressType().toString(),
                                    address.getRegion().split("/")[8],
                                    address.getStatus(),
                                    extractLastPathSegment(address.getSubnetwork()),
                                    extractLastPathSegment(address.getNetwork()),
                                    extractFirstUserResourceName(address)  // 연결된 리소스 이름 추출
                            );
                            staticIps.add(staticIpInfoDTO);
                        }
                    }
                }

                return staticIps;

            }

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * 주소가 할당된 첫 번째 리소스의 이름을 추출하는 메서드
     * @param address 고정 IP 주소 객체
     * @return 할당된 리소스의 이름, 없으면 빈 문자열
     */
    private String extractFirstUserResourceName(Address address) {
        if (!address.getUsersList().isEmpty()) {
            String userUrl = address.getUsersList().get(0);
            return extractLastPathSegment(userUrl);  // 첫 번째 할당된 리소스 이름 추출
        }
        return "";  // 할당된 리소스가 없으면 빈 문자열 반환
    }

    /**
     * URL에서 마지막 값을 추출하는 메서드
     * @param url URL 문자열
     * @return URL에서 마지막 부분 추출, 없으면 빈 문자열
     */
    private String extractLastPathSegment(String url) {
        if (url != null && !url.isEmpty()) {
            String[] parts = url.split("/");
            return parts[parts.length - 1];
        }
        return "";
    }



//    // Subnets 가져오기
//    public List<SubnetResponseDto> fetchSubnets(String userId) {
//
//        initializeClient(userId);
//        DescribeSubnetsRequest request = DescribeSubnetsRequest.builder().build();
//        DescribeSubnetsResponse response = ec2Client.describeSubnets(request);
//        return response.subnets().stream()
//                .map(subnet -> {
//                    Map<String, String> tagsMap = subnet.tags() == null ? Collections.emptyMap() :
//                            subnet.tags().stream().collect(Collectors.toMap(Tag::key, Tag::value));
//                    return new SubnetResponseDto(subnet.subnetId(), subnet.cidrBlock(), subnet.vpcId(), tagsMap, subnet.availabilityZone());
//                })
//                .collect(Collectors.toList());
//    }
//
//    // Route Tables 가져오기
//    public List<RouteTableResponseDto> fetchRouteTables(String userId) {
//        initializeClient(userId);
//        DescribeRouteTablesRequest request = DescribeRouteTablesRequest.builder().build();
//        DescribeRouteTablesResponse response = ec2Client.describeRouteTables(request);
//        return response.routeTables().stream()
//                .map(routeTable -> {
//                    Map<String, String> tagsMap = routeTable.tags() == null ? Collections.emptyMap() :
//                            routeTable.tags().stream().collect(Collectors.toMap(Tag::key, Tag::value));
//                    List<RouteDTO> routes = routeTable.routes().stream()
//                            .map(route -> new RouteDTO(route.gatewayId(), route.destinationCidrBlock()))
//                            .collect(Collectors.toList());
//                    return new RouteTableResponseDto(routeTable.routeTableId(), routeTable.vpcId(), routes, tagsMap);
//                })
//                .collect(Collectors.toList());
//    }
//
//    // VPCs 가져오기
//    public List<VpcTotalResponseDto> fetchVpcs(String userId) {
//
//        initializeClient(userId);
//
//        DescribeVpcsRequest request = DescribeVpcsRequest.builder().build();
//        DescribeVpcsResponse response = ec2Client.describeVpcs(request);
//
//        List<SubnetResponseDto> subnets = fetchSubnets(userId);
//        List<RouteTableResponseDto> routeTables = fetchRouteTables(userId);
//
//        return response.vpcs().stream()
//                .map(vpc -> {
//                    Map<String, String> tagsMap = vpc.tags() == null ? Collections.emptyMap() :
//                            vpc.tags().stream().collect(Collectors.toMap(Tag::key, Tag::value));
//
//                    List<SubnetResponseDto> associatedSubnets = subnets.stream()
//                            .filter(subnet -> subnet.getVpcId().equals(vpc.vpcId()))
//                            .collect(Collectors.toList());
//
//                    List<RouteTableResponseDto> associatedRouteTables = routeTables.stream()
//                            .filter(routeTable -> routeTable.getVpcId().equals(vpc.vpcId()))
//                            .collect(Collectors.toList());
//
//                    return new VpcTotalResponseDto(vpc.vpcId(), vpc.cidrBlock(), tagsMap, associatedSubnets, associatedRouteTables);
//                })
//                .collect(Collectors.toList());
//    }
//
//    // Security Groups 가져오기
//    public List<SecurityGroupDTO> fetchSecurityGroups(String userId) {
//
//        initializeClient(userId);
//        DescribeSecurityGroupsRequest request = DescribeSecurityGroupsRequest.builder().build();
//        DescribeSecurityGroupsResponse response = ec2Client.describeSecurityGroups(request);
//        return response.securityGroups().stream()
//                .map(group -> {
//                    Map<String, String> tagsMap = group.tags() == null ? Collections.emptyMap() :
//                            group.tags().stream().collect(Collectors.toMap(Tag::key, Tag::value));
//                    return new SecurityGroupDTO(group.groupId(), group.groupName(), tagsMap);
//                })
//                .collect(Collectors.toList());
//    }
//
//
//    public List<InternetGatewayDto> fetchInternetGateways(String userId) {
//
//        initializeClient(userId);
//        DescribeInternetGatewaysRequest request = DescribeInternetGatewaysRequest.builder().build();
//        DescribeInternetGatewaysResponse response = ec2Client.describeInternetGateways(request);
//
//        return response.internetGateways().stream()
//                .map(internetGateway -> {
//                    // 태그를 맵으로 변환
//                    Map<String, String> tagsMap = internetGateway.tags() == null ? Collections.emptyMap() :
//                            internetGateway.tags().stream().collect(Collectors.toMap(Tag::key, Tag::value));
//
//                    // Attachments의 정보를 적절하게 변환
//                    List<InternetGatewayDto.Attachment> attachments = internetGateway.attachments().stream()
//                            .map(attachment -> new InternetGatewayDto.Attachment(attachment.vpcId(), attachment.stateAsString())) // 상태를 문자열로 변환
//                            .collect(Collectors.toList());
//
//                    return new InternetGatewayDto(internetGateway.internetGatewayId(), tagsMap, attachments);
//                })
//                .collect(Collectors.toList());
//    }
//
//    // NAT Gateways 가져오기
//    public List<NatGatewayDto> fetchNatGateways(String userId) {
//        initializeClient(userId);
//        DescribeNatGatewaysRequest request = DescribeNatGatewaysRequest.builder().build();
//        DescribeNatGatewaysResponse response = ec2Client.describeNatGateways(request);
//
//        // ENI 정보를 가져옵니다.
//        List<NetworkInterfaceDto> networkInterfaces = fetchNetworkInterfaces(userId);
//        Map<String, List<NetworkInterfaceDto>> natGatewayEniMap = new HashMap<>();
//
//        // NAT Gateway와 연결된 ENI 정보를 매핑합니다.
//        for (NetworkInterfaceDto eni : networkInterfaces) {
//            for (var natGateway : response.natGateways()) {
//                for (var address : natGateway.natGatewayAddresses()) {
//                    if (address.networkInterfaceId() != null && address.networkInterfaceId().equals(eni.getNetworkInterfaceId())) {
//                        natGatewayEniMap
//                                .computeIfAbsent(natGateway.natGatewayId(), k -> new ArrayList<>())
//                                .add(eni);
//                    }
//                }
//            }
//        }
//
//        return response.natGateways().stream()
//                .map(natGateway -> {
//                    Map<String, String> tagsMap = natGateway.tags() == null ? Collections.emptyMap() :
//                            natGateway.tags().stream().collect(Collectors.toMap(Tag::key, Tag::value));
//
//                    // ENI ID 리스트 추출
//                    List<String> networkInterfaceIds = natGateway.natGatewayAddresses().stream()
//                            .map(address -> address.networkInterfaceId())
//                            .collect(Collectors.toList());
//
//                    // ENI DTO 리스트 가져오기
//                    List<NetworkInterfaceDto> eniList = natGatewayEniMap.getOrDefault(natGateway.natGatewayId(), Collections.emptyList());
//
//                    // NatGatewayDto 생성
//                    return new NatGatewayDto(
//                            natGateway.natGatewayId(),
//                            natGateway.stateAsString(),
//                            tagsMap,
//                            natGateway.vpcId(),
//                            eniList // 연결된 ENI DTO 리스트
//                    );
//                })
//                .collect(Collectors.toList());
//    }
//
//
//    public List<EipDto> fetchElasticIps(String userId) {
//        initializeClient(userId);
//        DescribeAddressesRequest request = DescribeAddressesRequest.builder().build();
//        DescribeAddressesResponse response = ec2Client.describeAddresses(request);
//
//        return response.addresses().stream()
//                .map(address -> {
//                    Map<String, String> tagsMap = address.tags() == null ? Collections.emptyMap() :
//                            address.tags().stream().collect(Collectors.toMap(Tag::key, Tag::value));
//
//                    return new EipDto(address.allocationId(), address.publicIp(), address.domain().name(), tagsMap);
//                })
//                .collect(Collectors.toList());
//    }
//
//
//
//    public List<NetworkInterfaceDto> fetchNetworkInterfaces(String userId) {
//        initializeClient(userId);
//        DescribeNetworkInterfacesRequest request = DescribeNetworkInterfacesRequest.builder().build();
//        DescribeNetworkInterfacesResponse response = ec2Client.describeNetworkInterfaces(request);
//
//        return response.networkInterfaces().stream()
//                .map(networkInterface -> {
//                    // 태그를 맵으로 변환
//                    Map<String, String> tagsMap = (networkInterface.tagSet() == null || networkInterface.tagSet().isEmpty())
//                            ? Collections.emptyMap()
//                            : networkInterface.tagSet().stream().collect(Collectors.toMap(Tag::key, Tag::value));
//
//                    List<String> privateIpAddresses = networkInterface.privateIpAddresses().stream()
//                            .map(ipAddress -> ipAddress.privateIpAddress())
//                            .collect(Collectors.toList());
//
//                    // Public IP 리스트 초기화
//                    List<String> publicIpAddresses = (networkInterface.association() != null && networkInterface.association().publicIp() != null)
//                            ? List.of(networkInterface.association().publicIp())
//                            : Collections.emptyList(); // Public IP가 없는 경우 빈 리스트
//
//                    return new NetworkInterfaceDto(
//                            networkInterface.networkInterfaceId(),
//                            networkInterface.subnetId(),
//                            networkInterface.vpcId(),
//                            networkInterface.statusAsString(),
//                            networkInterface.description(),
//                            tagsMap,
//                            privateIpAddresses,
//                            publicIpAddresses // Public IP 추가
//                    );
//                })
//                .collect(Collectors.toList());
//    }


}
