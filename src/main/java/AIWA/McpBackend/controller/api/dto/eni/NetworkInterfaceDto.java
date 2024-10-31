package AIWA.McpBackend.controller.api.dto.eni;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class NetworkInterfaceDto {
    private String networkInterfaceId; // ENI ID
    private String subnetId; // 서브넷 ID
    private String vpcId; // VPC ID
    private String status; // 상태 (예: "available", "in-use")
    private String description; // 설명
    private Map<String, String> tags; // 태그
    private List<String> privateIpAddresses;
    private List<String> publicIpAddresses;// 연결된 개인 IP 주소 목록
}