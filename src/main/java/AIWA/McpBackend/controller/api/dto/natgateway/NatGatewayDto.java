package AIWA.McpBackend.controller.api.dto.natgateway;

import AIWA.McpBackend.controller.api.dto.eni.NetworkInterfaceDto;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class NatGatewayDto {
    private String natGatewayId;
    private String state;
    private Map<String, String> tags;
    private String vpcId;
    private List<NetworkInterfaceDto> eniList; // ENI ID 리스트 추가

    public NatGatewayDto(String natGatewayId, String state, Map<String, String> tags, String vpcId,
                         List<NetworkInterfaceDto> eniList) {
        this.natGatewayId = natGatewayId;
        this.state = state;
        this.tags = tags;
        this.vpcId = vpcId;
        this.eniList = eniList;
    }
}