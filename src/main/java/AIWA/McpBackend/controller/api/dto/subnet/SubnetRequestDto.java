package AIWA.McpBackend.controller.api.dto.subnet;

import lombok.Data;

@Data
public class SubnetRequestDto {
    private String subnetName;    // Subnet 이름
    private String ipCidrRange;   // Subnet CIDR (예: 10.0.1.0/24)
    private String vpcName;       // 연결할 VPC 이름
}
