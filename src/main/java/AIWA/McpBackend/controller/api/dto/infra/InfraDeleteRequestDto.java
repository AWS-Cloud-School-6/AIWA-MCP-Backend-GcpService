package AIWA.McpBackend.controller.api.dto.infra;

import lombok.Data;
import java.util.List;

@Data
public class InfraDeleteRequestDto {
    private String vpcName;
    private List<String> subnetNames;
}