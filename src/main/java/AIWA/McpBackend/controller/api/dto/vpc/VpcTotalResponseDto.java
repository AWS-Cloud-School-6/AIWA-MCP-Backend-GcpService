// VpcDTO.java
package AIWA.McpBackend.controller.api.dto.vpc;

import AIWA.McpBackend.controller.api.dto.routetable.RouteTableResponseDto;
import AIWA.McpBackend.controller.api.dto.subnet.SubnetResponseDto;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class VpcTotalResponseDto {
    private final String vpcId;
    private final String cidr;
    private final Map<String, String> tags;
    private List<SubnetResponseDto> subnets; // 서브넷 리스트 추가
    private List<RouteTableResponseDto> routeTables; // 라우팅 테이블 리스트 추가


    public VpcTotalResponseDto(String vpcId, String cidr, Map<String, String> tags, List<SubnetResponseDto> subnets, List<RouteTableResponseDto> routeTables) {
        this.vpcId = vpcId;
        this.cidr = cidr;
        this.tags = tags;
        this.subnets = subnets;
        this.routeTables = routeTables;
    }

    public static VpcTotalResponseDto toDto(String vpcId, String cidr, Map<String, String> tags, List<SubnetResponseDto> subnets, List<RouteTableResponseDto> routeTables) {
        return new VpcTotalResponseDto(vpcId,cidr,tags,subnets,routeTables);
    }
}
