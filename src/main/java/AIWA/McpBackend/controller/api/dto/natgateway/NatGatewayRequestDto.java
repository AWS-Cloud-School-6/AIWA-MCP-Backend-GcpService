package AIWA.McpBackend.controller.api.dto.natgateway;

import lombok.Data;

@Data
public class NatGatewayRequestDto {
    private String natGatewayName;  // NAT Gateway의 이름
    private String subnetName;      // NAT Gateway가 연결될 Subnet 이름
    private String elasticIpName;   // NAT Gateway에 연결될 Elastic IP 이름
    private String allocationId;
}