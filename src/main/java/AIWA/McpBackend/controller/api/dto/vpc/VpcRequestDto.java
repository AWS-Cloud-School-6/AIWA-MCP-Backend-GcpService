package AIWA.McpBackend.controller.api.dto.vpc;

import lombok.Data;

@Data
public class VpcRequestDto {

    private String vpcName;
    private String cidrBlock;
    // 필요한 추가 필드들 (예: 태그 등)

    // Getters and Setters
}
