// VpcDTO.java
package AIWA.McpBackend.controller.api.dto.vpc;

import AIWA.McpBackend.controller.api.dto.routetable.RouteTableResponseDto;
import AIWA.McpBackend.controller.api.dto.subnet.SubnetResponseDto;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class VpcTotalResponseDto {
    private String vpcId;
    private String vpcName;
    private List<String> subnets;
    private List<String> routingTables;

    // Constructor, Getter, Setter
    public VpcTotalResponseDto(String vpcId, String vpcName, List<String> subnets, List<String> routingTables) {
        this.vpcId = vpcId;
        this.vpcName = vpcName;
        this.subnets = subnets;
        this.routingTables = routingTables;
    }

}
