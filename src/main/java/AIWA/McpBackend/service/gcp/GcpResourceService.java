package AIWA.McpBackend.service.gcp;

import AIWA.McpBackend.controller.api.dto.cloudrouter.CloudRouterDto;
import AIWA.McpBackend.controller.api.dto.cloudnat.CloudNatDto;
import AIWA.McpBackend.controller.api.dto.cloudrouter.LogConfigDto;
import AIWA.McpBackend.controller.api.dto.cloudrouter.RouterNatDto;
import AIWA.McpBackend.controller.api.dto.response.ListResult;
import AIWA.McpBackend.controller.api.dto.routetable.RoutePolicyDto;
import AIWA.McpBackend.controller.api.dto.firewallpolicy.FireWallPolicyDto;
import AIWA.McpBackend.controller.api.dto.staticip.StaticIpDto;
import AIWA.McpBackend.controller.api.dto.subnet.SubnetResponseDto;
import AIWA.McpBackend.controller.api.dto.vm.NetworkInterfaceDto;
import AIWA.McpBackend.controller.api.dto.vm.VmResponseDto;
import AIWA.McpBackend.controller.api.dto.vpc.VpcTotalResponseDto;
import AIWA.McpBackend.service.gcp.s3.S3Service;
import AIWA.McpBackend.service.response.ResponseService;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.paging.Page;
import com.google.cloud.compute.v1.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class GcpResourceService {

    @Autowired
    private ResponseService responseService;
    private final RestTemplate restTemplate;
    private S3Service s3Service;

    private GoogleCredentials getCredentials(String userId) throws IOException {
        //credential파일 반환
        String fileName = s3Service.downloadJsonFile(userId);
        // JSON 키 파일 경로 설정
        String credentialsPath = "tmp/"+fileName;
        return GoogleCredentials.fromStream(new FileInputStream(credentialsPath))
                .createScoped("https://www.googleapis.com/auth/cloud-platform");
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

    //vm 조회
    public ResponseEntity<?> listInstances(String projectId, String userId) {
        List<VmResponseDto> instanceList = new ArrayList<>();

        try {
            // JSON 키 파일 경로 설정
            GoogleCredentials credentials = getCredentials(userId);

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

                            // 네트워크 인터페이스 정보 추출
                            List<NetworkInterfaceDto> networkInterfaces = new ArrayList<>();
                            for (NetworkInterface networkInterface : instance.getNetworkInterfacesList()) {
                                String network = networkInterface.getName();
                                String internalIp = networkInterface.getNetworkIP();
                                String externalIp = networkInterface.getAccessConfigsList().isEmpty() ?
                                        null : networkInterface.getAccessConfigs(0).getNatIP();
                                networkInterfaces.add(new NetworkInterfaceDto(network, internalIp, externalIp));
                            }

                            // VmResponseDto 객체에 인스턴스 정보 추가
                            instanceList.add(new VmResponseDto(name, status, zone, networkInterfaces));
                        }
                    }
                });

                // 인스턴스가 없는 경우 빈 목록을 반환
                return ResponseEntity.ok(responseService.getListResult(instanceList));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(responseService.getFailResult("VPC creation failed: " + e.getMessage()));
        }
    }

    //subnet 조회
    public ListResult<SubnetResponseDto> listSubnets(String projectId, String userId) throws IOException {
        // GCP 인증 처리 (서비스 계정 키 파일을 사용하여 인증)
        GoogleCredentials credentials = getCredentials(userId);

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
    public ResponseEntity<?> listVpcsWithDetails(String projectId, String userId) {
        List<VpcTotalResponseDto> vpcList = new ArrayList<>();

        try {
            // 인증 처리
            GoogleCredentials credentials = getCredentials(userId);

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
            return ResponseEntity.status(500).body(responseService.getFailResult("VPC creation failed: " + e.getMessage()));
        }
    }

    //static ip 조회
    public List<StaticIpDto> getStaticIpsFromGCP(String projectId, String userId) {
        String region = "us-central1";

        try {
            GoogleCredentials credentials = getCredentials(userId);

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



    //FireWallPolicy 조회
    public ListResult<FireWallPolicyDto> getFirewallRules(String projectId, String userId) {
        List<FireWallPolicyDto> firewallPolicies = new ArrayList<>();

        try {
            // 인증 정보를 설정
            GoogleCredentials credentials = getCredentials(userId);

            // 인증 정보를 적용하여 FirewallsClient를 생성
            FirewallsSettings firewallsSettings = FirewallsSettings.newBuilder()
                    .setCredentialsProvider(() -> credentials)
                    .build();

            // FirewallsClient를 사용하여 방화벽 규칙 가져오기
            try (FirewallsClient firewallsClient = FirewallsClient.create(firewallsSettings)) {
                for (Firewall firewall : firewallsClient.list(projectId).iterateAll()) {
                    // target이 빈 배열일 경우 배열에 *를 넣음
                    List<String> target = (firewall.getTargetTagsList() == null || firewall.getTargetTagsList().isEmpty())
                            ? Arrays.asList("*")  // 전체 대상을 의미하는 배열에 * 넣기
                            : firewall.getTargetTagsList();  // 실제 값 처리

                    // sourceRanges가 비어 있으면 배열에 *를 넣음
                    List<String> sourceRanges = (firewall.getSourceRangesList() != null && !firewall.getSourceRangesList().isEmpty())
                            ? firewall.getSourceRangesList()  // 실제 값 처리
                            : Arrays.asList("*");  // 비어 있으면 배열에 * 넣기

                    // protocolPorts를 적절히 처리
                    List<String> protocolPorts = (firewall.getAllowedList() != null && !firewall.getAllowedList().isEmpty())
                            ? firewall.getAllowedList().stream()
                            .map(allowed -> allowed.getIPProtocol() + ":" + String.join(",", allowed.getPortsList()))
                            .collect(Collectors.toList())  // 프로토콜과 포트를 배열로 수집
                            : Arrays.asList("*");  // 비어 있으면 배열에 * 넣기

                    // LogConfig의 enable 값을 처리: Boolean로 반환되므로 null 체크
                    Boolean logEnabled = (firewall.getLogConfig() != null) ? firewall.getLogConfig().getEnable() : Boolean.FALSE;

                    // FireWallPolicyDto로 변환
                    FireWallPolicyDto firewallPolicy = new FireWallPolicyDto(
                            firewall.getName(),
                            firewall.getDirection(),
                            target.toString(),  // 배열을 문자열로 변환하여 전달
                            sourceRanges.toString(),  // 배열을 문자열로 변환하여 전달
                            protocolPorts.toString(),  // 배열을 문자열로 변환하여 전달
                            logEnabled,
                            firewall.getPriority(),
                            extractLastPathSegment(firewall.getNetwork())
                    );

                    firewallPolicies.add(firewallPolicy);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return responseService.getListResult(new ArrayList<>()); // 실패 시 빈 리스트 반환
        }

        return responseService.getListResult(firewallPolicies); // 성공 시 방화벽 정책 리스트 반환
    }

    public List<RoutePolicyDto> fetchRouteTables(String projectId, String userId) {
        List<RoutePolicyDto> routePolicies = new ArrayList<>();

        try {
            // GoogleCredentials 가져오기
            GoogleCredentials credentials = getCredentials(userId);

            // RoutesClient 생성 시 인증 정보 설정
            RoutesSettings routesSettings = RoutesSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();

            try (RoutesClient routesClient = RoutesClient.create(routesSettings)) {
                // 프로젝트 ID로 라우트 테이블 가져오기
                RoutesClient.ListPagedResponse response = routesClient.list(projectId);
                Page<Route> routes = response.getPage();

                // 라우트를 순회하여 데이터를 처리
                for (Route route : routes.iterateAll()) {
                    String name = route.getName();
                    String destinationRange = route.getDestRange();
                    String nextHop = route.getNextHopGateway();
                    String lastPathSegment = "";
                    int priority = route.getPriority();

                    if (nextHop != null && !nextHop.isEmpty()) {
                        // nextHop 값이 있을 경우 마지막 경로 세그먼트 추출
                        lastPathSegment = extractLastPathSegment(nextHop);
                    }

                    // RoutePolicyDto 객체 생성
                    RoutePolicyDto routePolicy = new RoutePolicyDto(
                            name,
                            destinationRange,
                            lastPathSegment,
                            priority
                    );

                    routePolicies.add(routePolicy);
                }
            }
        } catch (IOException e) {
            // 오류 발생 시 로그 출력 및 RuntimeException 던지기
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch route tables: " + e.getMessage());
        }

        return routePolicies; // List<RoutePolicyDto> 반환
    }

    public List<CloudNatDto> fetchCloudNatDetails(String projectId, String region, String userId) {
        List<CloudNatDto> cloudNatDetails = new ArrayList<>();

        try {
            // GoogleCredentials 가져오기
            GoogleCredentials credentials = getCredentials(userId);

            // RoutersClient 생성 시 인증 정보 설정
            RoutersSettings routersSettings = RoutersSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();

            try (RoutersClient routersClient = RoutersClient.create(routersSettings)) {
                // 프로젝트와 리전으로 Router 목록 가져오기
                for (Router router : routersClient.list(projectId, region).iterateAll()) {
                    String routerName = router.getName();
                    String network = extractLastPathSegment(router.getNetwork());
                    String routerRegion = region; // 전달된 리전을 그대로 사용
                    String status = "ACTIVE"; // 기본값, Router 상태 조회는 API에서 직접 확인 필요

                    // NAT 정보 가져오기
                    List<RouterNat> natList = router.getNatsList();
                    for (RouterNat nat : natList) {
                        String natName = nat.getName();
                        String natType = nat.getType(); // NAT 유형 추출
                        String cloudRouter = routerName; // NAT과 연결된 Cloud Router 이름

                        // DTO 생성
                        cloudNatDetails.add(new CloudNatDto(
                                natName,
                                network,
                                routerRegion,
                                natType,
                                cloudRouter,
                                status
                        ));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch Cloud NAT details: " + e.getMessage());
        }

        return cloudNatDetails;
    }

    public List<CloudRouterDto> fetchCloudRouterInfo(String projectId, String region, String userId) {
        List<CloudRouterDto> cloudRouterDtos = new ArrayList<>();

        try {
            // GoogleCredentials 가져오기
            GoogleCredentials credentials = getCredentials(userId);

            // RoutersClient 생성 시 인증 정보 설정
            RoutersSettings routersSettings = RoutersSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();

            try (RoutersClient routersClient = RoutersClient.create(routersSettings)) {
                // 프로젝트와 리전으로 Router 목록 가져오기
                ListRoutersRequest listRoutersRequest = ListRoutersRequest.newBuilder()
                        .setProject(projectId)
                        .setRegion(region)
                        .build();

                // Router 목록 순회
                for (Router router : routersClient.list(listRoutersRequest).iterateAll()) {
                    String routerName = router.getName();
                    String network = extractLastPathSegment(router.getNetwork());
                    String routerRegion = region; // 전달된 리전을 그대로 사용
                    boolean encryptedInterconnectRouter = router.getEncryptedInterconnectRouter();
                    String googleAsn = null; // Google ASN 정보가 없으므로 기본값 null
                    String interconnectVpnGateway = ""; // 기본값
                    String connection = ""; // 기본값
                    String bgpSession = ""; // 기본값

                    // NAT 정보가 있으면 NAT 정보에서 추출
                    List<RouterNatDto> routerNatDtos = new ArrayList<>();
                    List<RouterNat> natList = router.getNatsList();
                    for (RouterNat nat : natList) {
                        String natName = nat.getName();
                        String natType = nat.getType(); // NAT 유형 추출
                        String sourceSubnetworkIpRangesToNat = nat.getSourceSubnetworkIpRangesToNat();
                        boolean enableEndpointIndependentMapping = nat.getEnableEndpointIndependentMapping();
                        String autoNetworkTier = nat.getAutoNetworkTier();
                        LogConfigDto logConfig = new LogConfigDto(nat.getLogConfig().getEnable(), nat.getLogConfig().getFilter());
                        String natIpAllocateOption = nat.getNatIpAllocateOption();
                        List<String> endpointTypes = nat.getEndpointTypesList();
                        boolean enableDynamicPortAllocation = nat.getEnableDynamicPortAllocation();

                        // NAT 정보를 RouterNatDto로 변환하여 리스트에 추가
                        routerNatDtos.add(new RouterNatDto(
                                natName,
                                natType,
                                sourceSubnetworkIpRangesToNat,
                                enableEndpointIndependentMapping,
                                autoNetworkTier,
                                logConfig,
                                natIpAllocateOption,
                                endpointTypes,
                                enableDynamicPortAllocation
                        ));

                        // NAT 관련 정보로 interconnectVpnGateway, connection 값 설정
                        interconnectVpnGateway = natIpAllocateOption != null ? natIpAllocateOption : "";
                        connection = endpointTypes.isEmpty() ? "" : String.join(", ", endpointTypes);
                        if (interconnectVpnGateway == null || interconnectVpnGateway.isEmpty() || "AUTO_ONLY".equals(interconnectVpnGateway)) {
                            interconnectVpnGateway = "";
                        }
                        if (connection == null || connection.isEmpty() || "ENDPOINT_TYPE_VM".equals(connection)) {
                            connection = "";
                        }
                    }

                    // CloudRouterDto 생성
                    cloudRouterDtos.add(new CloudRouterDto(
                            routerName,
                            network,
                            routerRegion,
                            encryptedInterconnectRouter ? "true" : "false", // 암호화 여부 표시
                            googleAsn,
                            interconnectVpnGateway,
                            connection,
                            bgpSession, // BGP 세션 정보는 없으므로 N/A
                            routerNatDtos // NAT 리스트 추가
                    ));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch Cloud Router details: " + e.getMessage());
        }

        return cloudRouterDtos;
    }
}
