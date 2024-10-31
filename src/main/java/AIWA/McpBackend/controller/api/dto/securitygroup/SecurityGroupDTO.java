// SecurityGroupDTO.java
package AIWA.McpBackend.controller.api.dto.securitygroup;

import lombok.Getter;

import java.util.Map;

@Getter
public class SecurityGroupDTO {
    private final String groupId;
    private final String groupName;
    private final Map<String, String> tags;

    public SecurityGroupDTO(String groupId, String groupName, Map<String, String> tags) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.tags = tags;
    }

}
