// SubnetDTO.java
package AIWA.McpBackend.controller.api.dto.subnet;

import lombok.Getter;

import java.util.Map;

@Getter
public class SubnetResponseDto {
    private final String subnetId;
    private final String cidr;
    private final String vpcId;
    private final Map<String, String> tags;
    private final String availabilityZone;

    public SubnetResponseDto(String subnetId, String cidr, String vpcId, Map<String, String> tags, String availabilityZone) {
        this.subnetId = subnetId;
        this.cidr = cidr;
        this.vpcId = vpcId;
        this.tags = tags;
        this.availabilityZone = availabilityZone;
    }

}
