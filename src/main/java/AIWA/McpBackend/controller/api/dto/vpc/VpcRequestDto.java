package AIWA.McpBackend.controller.api.dto.vpc;

import lombok.Data;

@Data
public class VpcRequestDto {

    private String vpcName;
    private String cidrBlock;
    private String region; // 서브넷이 위치할 리전 설정 (GCP용)

    // 필요한 추가 필드들 (예: 태그 등)

    // Getters and Setters
}