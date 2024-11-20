package AIWA.McpBackend.controller.api.dto.cloudrouter;

import lombok.Getter;

@Getter
public class LogConfigDto {
    private boolean enable;
    private String filter;

    // 생성자
    public LogConfigDto(boolean enable, String filter) {
        this.enable = enable;
        this.filter = filter;
    }
}
