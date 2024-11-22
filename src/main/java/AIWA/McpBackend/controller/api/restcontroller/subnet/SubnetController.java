package AIWA.McpBackend.controller.api.restcontroller.subnet;

import AIWA.McpBackend.controller.api.dto.response.CommonResult;
import AIWA.McpBackend.controller.api.dto.response.ListResult;
import AIWA.McpBackend.controller.api.dto.subnet.SubnetRequestDto;
import AIWA.McpBackend.controller.api.dto.subnet.SubnetResponseDto;
import AIWA.McpBackend.service.gcp.subnet.SubnetService;
import AIWA.McpBackend.service.gcp.GcpResourceService;
import AIWA.McpBackend.service.response.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gcp/api/subnet")
@RequiredArgsConstructor
public class SubnetController {

    private final SubnetService subnetService;
    private final GcpResourceService gcpResourceService;
    private final ResponseService responseService;

    /**
     * Subnet 생성 엔드포인트
     *
     * @param subnetRequest Subnet 생성 요청 DTO
     * @param userId        사용자 ID (요청 파라미터로 전달)
     * @return 생성 성공 메시지 또는 오류 메시지
     */
    @PostMapping("/create")
    public CommonResult createSubnet(
            @RequestBody SubnetRequestDto subnetRequest,
            @RequestParam String userId) {
        try {
            subnetService.createSubnet(subnetRequest, userId);
            return responseService.getSuccessResult();
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("Subnet creation failed: " + e.getMessage());
        }
    }

    /**
     * Subnet 삭제 엔드포인트
     *
     * @param subnetName Subnet 이름 (요청 파라미터로 전달)
     * @param userId     사용자 ID (요청 파라미터로 전달)
     * @return 삭제 성공 메시지 또는 오류 메시지
     */
    @DeleteMapping("/delete")
    public CommonResult deleteSubnet(
            @RequestParam String subnetName,
            @RequestParam String userId) {
        try {
            subnetService.deleteSubnet(subnetName, userId);
            return responseService.getSuccessResult();
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("Subnet deletion failed: " + e.getMessage());
        }
    }

    /**
     * GCP 서브넷 조회 API
     *
     * @param projectId GCP 프로젝트 ID
     * @return 서브넷 목록을 ListResult로 반환
     */
    @GetMapping("/describe")
    public ListResult<SubnetResponseDto> listSubnets(@RequestParam String projectId, @RequestParam String userId) {
        try {
            // GcpResourceService를 통해 서브넷 리스트 조회
            return gcpResourceService.listSubnets(projectId, userId);
        } catch (Exception e) {
            // 예외 발생 시 실패 결과 반환
            e.printStackTrace();
            return responseService.getListResult(null);  // 실패 시 null 반환 또는 적절한 실패 메시지 반환
        }
    }
}
