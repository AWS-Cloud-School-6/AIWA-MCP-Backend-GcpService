package AIWA.McpBackend.controller.api.dto.routetable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RouteTableRequestDto {
    private String name;               // 라우트 이름
    private String destinationRange;   // 목적지 CIDR (예: 10.0.0.0/16)
    private String networkName;        // 연결할 네트워크 이름
    private String nextHop;            // 다음 홉 게이트웨이
    private int priority;              // 우선순위

    // 생성자 (이 객체를 생성할 때 사용할 수 있음)
    public RouteTableRequestDto(String name, String destinationRange, String networkName, String nextHop, int priority) {
        this.name = name;
        this.destinationRange = destinationRange;
        this.networkName = networkName;
        this.nextHop = nextHop;
        this.priority = priority;
    }

    // 기본 생성자
    public RouteTableRequestDto() {}
}
