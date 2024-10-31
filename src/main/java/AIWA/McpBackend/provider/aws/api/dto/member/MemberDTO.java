package AIWA.McpBackend.provider.aws.api.dto.member;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class MemberDTO {
    private String accessKey;
    private String secretKey;
    // 다른 필드 추가 가능
}