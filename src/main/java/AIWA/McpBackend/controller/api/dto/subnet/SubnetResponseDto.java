// SubnetDTO.java
package AIWA.McpBackend.controller.api.dto.subnet;

import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class SubnetResponseDto {
    private final String subnetId;
    private final String cidr;
    private final String vpcId;
    private final List<String> tags;
    private final String region;
    private final String subnetName;

    public SubnetResponseDto(String subnetId, String cidr, String vpcId, List<String> tags, String region, String subnetName) {
        this.subnetId = subnetId;
        this.cidr = cidr;
        this.vpcId = vpcId;
        this.tags = tags;
        this.region = region;
        this.subnetName = subnetName;
    }

}
