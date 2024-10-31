package AIWA.McpBackend.controller.api.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SingleResult<T>extends CommonResult { //결과가 단일건인 API를 담는 모델
    private T data;
}
