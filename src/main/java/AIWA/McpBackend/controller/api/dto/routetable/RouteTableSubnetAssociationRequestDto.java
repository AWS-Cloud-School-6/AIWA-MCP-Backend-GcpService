package AIWA.McpBackend.controller.api.dto.routetable;

import lombok.Data;

@Data
public class RouteTableSubnetAssociationRequestDto {
    private String routeTableName;  // 라우트 테이블 이름
    private String subnetName;      // 서브넷 이름
    private String userId;          // 사용자 ID
}