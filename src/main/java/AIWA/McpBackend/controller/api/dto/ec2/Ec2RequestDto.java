package AIWA.McpBackend.controller.api.dto.ec2;

import lombok.Getter;

@Getter
public class Ec2RequestDto {
    private String instanceName;    // EC2 인스턴스 이름
    private String instanceType;    // EC2 인스턴스 타입 (예: t2.micro)
    private String amiId;           // Amazon Machine Image (AMI) ID
    private String subnetName;      // EC2 인스턴스가 연결될 Subnet 이름
    private String securityGroupName; // EC2 인스턴스에 연결될 Security Group 이름
    private String keyPairName;     // EC2 인스턴스에 연결될 키페어 이름 (SSH 접속용)
}