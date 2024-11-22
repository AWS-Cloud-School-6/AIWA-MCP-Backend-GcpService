package AIWA.McpBackend.controller.api.restcontroller.staticip;

import AIWA.McpBackend.controller.api.dto.response.ListResult;
import AIWA.McpBackend.controller.api.dto.staticip.StaticIpDto;
import AIWA.McpBackend.controller.api.dto.staticip.StaticIpRequestDto;
import AIWA.McpBackend.controller.api.dto.response.CommonResult;
import AIWA.McpBackend.service.gcp.GcpResourceService;
import AIWA.McpBackend.service.gcp.staticip.StaticIpService;
import AIWA.McpBackend.service.response.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gcp/api/staticip")
@RequiredArgsConstructor
public class StaticIpController {

    private final StaticIpService staticIpService;

    private final GcpResourceService gcpResourceService;
    private final ResponseService responseService;

    /**
     * Static IP 생성 엔드포인트
     *
     * @param staticIpRequest Static IP 생성 요청 DTO
     * @param userId          사용자 ID
     * @return 생성 성공 메시지 또는 오류 메시지
     */
    @PostMapping("/create")
    public CommonResult createStaticIp(
            @RequestBody StaticIpRequestDto staticIpRequest,
            @RequestParam String userId) {
        try {
            // StaticIpService에서 직접 호출하여 Static IP 생성
            staticIpService.createStaticIp(staticIpRequest, userId);
            return responseService.getSuccessResult();
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("Static IP creation failed: " + e.getMessage());
        }
    }

    /**
     * Static IP 삭제 엔드포인트
     *
     * @param ipName Static IP 이름
     * @param userId 사용자 ID
     * @return 삭제 성공 메시지 또는 오류 메시지
     */
    @DeleteMapping("/delete")
    public CommonResult deleteStaticIp(
            @RequestParam String ipName,
            @RequestParam String userId) {
        try {
            // StaticIpService에서 직접 호출하여 Static IP 삭제
            staticIpService.deleteStaticIp(ipName, userId);
            return responseService.getSuccessResult();
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("Static IP deletion failed: " + e.getMessage());
        }
    }


    /**
     * Static IP 목록 조회 API
     *
     * @param projectId GCP 프로젝트 ID
     * @return Static IP 목록을 ListResult로 반환
     */
    @GetMapping("/describe")
    public ListResult<StaticIpDto> getStaticIps(
            @RequestParam String projectId,
            @RequestParam String userId) {
        try {
            // GCP에서 Static IP 목록 조회
            List<StaticIpDto> staticIps = gcpResourceService.getStaticIpsFromGCP(projectId, userId);

            // ListResult 형태로 반환
            return responseService.getListResult(staticIps);
        } catch (Exception e) {
            // 예외 발생 시 실패 결과 반환
            e.printStackTrace();
            return responseService.getListResult(null);  // 실패 시 null 반환
        }
    }
}
