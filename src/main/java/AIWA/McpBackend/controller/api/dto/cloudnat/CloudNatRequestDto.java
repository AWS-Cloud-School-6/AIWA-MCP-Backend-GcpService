package AIWA.McpBackend.controller.api.dto.cloudnat;

import lombok.Getter;

@Getter
public class CloudNatRequestDto {
    private String region;
    private String network;
    private String routerName;
    private String natName;

    public CloudNatRequestDto(String region, String network, String routerName, String natName) {
        this.region = region;
        this.network = network;
        this.routerName = routerName;
        this.natName = natName;
    }
}
