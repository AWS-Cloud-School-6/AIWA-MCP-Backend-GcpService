package AIWA.McpBackend.controller.api.dto.internetgateway;


import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;
import java.util.Map;


@Data
@AllArgsConstructor
public class InternetGatewayDto {
    private String internetGatewayId; // Internet Gateway ID
    private Map<String, String> tags; // 태그
    private List<Attachment> attachments; // 첨부된 VPC 정보

    // 첨부된 VPC 정보를 저장하는 내부 클래스
    @Data
    @AllArgsConstructor
    public static class Attachment {
        private String vpcId; // VPC ID
        private String state; // 상태 (예: "attached", "detached")
    }
}