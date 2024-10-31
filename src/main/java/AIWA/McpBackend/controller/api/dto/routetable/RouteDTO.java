package AIWA.McpBackend.controller.api.dto.routetable;

import lombok.Getter;

@Getter
public class RouteDTO {
    private String gatewayId;
    private String destinationCidrBlock;

    public RouteDTO(String gatewayId, String destinationCidrBlock) {
        this.gatewayId = gatewayId;
        this.destinationCidrBlock = destinationCidrBlock;
    }

    // Getters and setters
}
