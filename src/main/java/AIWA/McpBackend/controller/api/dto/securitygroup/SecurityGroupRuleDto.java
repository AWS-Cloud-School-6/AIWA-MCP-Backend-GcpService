package AIWA.McpBackend.controller.api.dto.securitygroup;

import lombok.Data;

@Data
public class SecurityGroupRuleDto {
    private String protocol;           // 프로토콜 (예: tcp, udp, icmp)
    private String fromPort;           // 시작 포트
    private String toPort;             // 종료 포트
    private String cidrBlock;          // CIDR 블록 (예: 0.0.0.0/0)
}