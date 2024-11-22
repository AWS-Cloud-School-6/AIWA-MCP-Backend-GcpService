package AIWA.McpBackend.controller.api.dto.vm;

import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@Getter
public class VmResponseDto {
    private String name;
    private String status;
    private String zone;
    private List<NetworkInterfaceDto> networkInterfaces;

    public VmResponseDto(String name, String status, String zone, List<NetworkInterfaceDto> networkInterfaces) {
        this.name = name;
        this.status = status;
        this.zone = zone;
        this.networkInterfaces = networkInterfaces;
    }
}
