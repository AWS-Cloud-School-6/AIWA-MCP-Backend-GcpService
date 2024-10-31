package AIWA.McpBackend.controller.api.dto.subnet;


import lombok.Data;

@Data
public class SubnetRequestDto {
    private String subnetName;
    private String vpcName;  // Subnet을 생성할 VPC 이름
    private String cidrBlock;
    private String availabilityZone;
}