package AIWA.McpBackend.controller.api.restcontroller.vm;

import AIWA.McpBackend.controller.api.dto.response.CommonResult;
import AIWA.McpBackend.controller.api.dto.vm.VmRequestDto;
import AIWA.McpBackend.service.gcp.GcpResourceService;
import AIWA.McpBackend.service.gcp.vm.VmService;
import AIWA.McpBackend.service.response.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gcp/api/vm")
@RequiredArgsConstructor
public class VmController {

    private final VmService vmService;

    private final ResponseService responseService;
    private final GcpResourceService gcpResourceService;


    /**
     * GCP VM 생성 엔드포인트
     *
     * @param vmRequest VM 생성 요청 DTO
     * @param userId    사용자 ID
     * @return 생성 성공 메시지 또는 오류 메시지
     */
    @PostMapping("/create")
    public CommonResult createVm(
            @RequestBody VmRequestDto vmRequest,
            @RequestParam String userId,
            @RequestParam String projectId) {
        try {
            // VMService에서 직접 호출하여 VM 생성
            vmService.createVm(vmRequest, userId,projectId);
            return responseService.getSuccessResult();
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("VM creation failed: " + e.getMessage());
        }
    }

    /**
     * GCP VM 삭제 엔드포인트
     *
     * @param vmName VM 이름
     * @param userId 사용자 ID
     * @return 삭제 성공 메시지 또는 오류 메시지
     */
    @DeleteMapping("/delete")
    public CommonResult deleteVm(
            @RequestParam String vmName,
            @RequestParam String userId) {
        try {
            // VMService에서 직접 호출하여 VM 삭제
            vmService.deleteVm(vmName, userId);
            return responseService.getSuccessResult();
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("VM deletion failed: " + e.getMessage());
        }
    }


    @GetMapping("/describe")
    public ResponseEntity<?> listInstances(@RequestParam String projectId, @RequestParam String userId) {
        return gcpResourceService.listInstances(projectId, userId);
    }
}