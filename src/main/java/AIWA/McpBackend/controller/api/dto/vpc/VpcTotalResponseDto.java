package AIWA.McpBackend.controller.api.dto.vpc;

import lombok.Data;
import java.util.List;

@Data
public class VpcTotalResponseDto {
    private String networkId;
    private String name;
    private String selfLink;
    private List<String> subnetworks;

    public VpcTotalResponseDto(String networkId, String name, String selfLink, List<String> subnetworks) {
        this.networkId = networkId;
        this.name = name;
        this.selfLink = selfLink;
        this.subnetworks = subnetworks;
    }
}