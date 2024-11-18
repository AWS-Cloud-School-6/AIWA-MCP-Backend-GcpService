package AIWA.McpBackend.controller.api.dto.routetable;

import lombok.Getter;

@Getter
public class RoutePolicyDto {
    private String name;
    private String destinationRange;
    private String nextHop;
    private int priority;


    public RoutePolicyDto(String name, String destinationRange, String nextHop, int priority) {
        this.name = name;
        this.destinationRange = destinationRange;
        this.nextHop = nextHop;
        this.priority = priority;
    }
}
