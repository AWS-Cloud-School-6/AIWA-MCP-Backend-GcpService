package AIWA.McpBackend.controller.api.dto.vm;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class VmResponseDto {
    private String name;
    private String status;
    private String zone;
    private String externalIp;
    private String internalIp;

    public VmResponseDto(String name, String status, String zone, String externalIp, String internalIp) {
        this.name = name;
        this.status = status;
        this.zone = zone;
        this.externalIp = externalIp;
        this.internalIp = internalIp;
    }
}
