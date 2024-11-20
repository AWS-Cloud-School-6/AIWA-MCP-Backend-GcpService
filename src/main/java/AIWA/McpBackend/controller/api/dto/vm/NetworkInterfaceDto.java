package AIWA.McpBackend.controller.api.dto.vm;

import lombok.Getter;

@Getter
public class NetworkInterfaceDto {
    private String name;
    private String internalIp;
    private String externalIp;

    public NetworkInterfaceDto(String name, String internalIp, String externalIp) {
        this.name = name;
        this.internalIp = internalIp;
        this.externalIp = externalIp;
    }
}
