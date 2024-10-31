package AIWA.McpBackend.controller.api.dto.membercredential;

import lombok.Data;
import lombok.Getter;


@Data
@Getter
public class MemberCredentialDTO {
    private String email;
    private String accessKey;
    private String secretKey;

    public MemberCredentialDTO(String email, String accessKey, String secretKey) {
        this.email = email;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }
}