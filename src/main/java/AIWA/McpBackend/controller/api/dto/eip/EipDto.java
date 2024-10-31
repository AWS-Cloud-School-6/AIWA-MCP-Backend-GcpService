package AIWA.McpBackend.controller.api.dto.eip;

import lombok.Data;

import java.util.Map;

@Data
public class EipDto {
    private String allocationId; // EIP 할당 ID
    private String publicIp; // 공인 IP 주소
    private String domain; // 도메인 (예: "vpc" 또는 "standard")
    private Map<String, String> tags; // 태그

    public EipDto(String allocationId, String publicIp, String domain, Map<String, String> tags) {
        this.allocationId = allocationId;
        this.publicIp = publicIp;
        this.domain = domain;
        this.tags = tags;
    }
}
