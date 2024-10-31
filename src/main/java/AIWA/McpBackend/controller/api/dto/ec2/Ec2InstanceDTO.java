// EC2InstanceDTO.java
package AIWA.McpBackend.controller.api.dto.ec2;

import lombok.Getter;

import java.util.Map;

@Getter
public class Ec2InstanceDTO {
    private final String instanceId;
    private final String state;
    private final Map<String, String> tags;

    public Ec2InstanceDTO(String instanceId, String state, Map<String, String> tags) {
        this.instanceId = instanceId;
        this.state = state;
        this.tags = tags;
    }

}
