package AIWA.McpBackend.controller.api.dto.routetable;

import lombok.Data;

@Data
public class RouteAddRequestDto {
    private String routeTableName;    // 라우트 테이블 이름
    private String destinationCidr;   // 목적지 CIDR
    private String gatewayType;       // 게이트웨이 유형 (인터넷 게이트웨이, NAT 게이트웨이, VPC 피어링 등)
    private String gatewayId;         // 게이트웨이 또는 피어링의 ID
    private String userId;            // 사용자 ID
}