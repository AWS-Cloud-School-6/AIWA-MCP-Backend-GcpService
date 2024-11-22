package AIWA.McpBackend.controller.api.restcontroller.vpc;

import AIWA.McpBackend.controller.api.dto.vpc.VpcRequestDto;
import AIWA.McpBackend.controller.api.dto.response.CommonResult;
import AIWA.McpBackend.service.gcp.vpc.VpcService;  // Added missing VpcService import
import AIWA.McpBackend.service.gcp.GcpResourceService;
import AIWA.McpBackend.service.response.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gcp/api/vpc")
@RequiredArgsConstructor
public class VpcController {

    private final VpcService vpcService;  // VpcService 사용
    private final GcpResourceService gcpResourceService;
    private final ResponseService responseService;

    /**
     * VPC 생성 엔드포인트
     *
     * @param vpcRequest VPC 생성 요청 DTO
     * @param userId     사용자 ID (요청 파라미터로 전달)
     * @return 생성 성공 메시지 또는 오류 메시지
     */
    @PostMapping("/create")
    public CommonResult createVpc(
            @RequestBody VpcRequestDto vpcRequest,
            @RequestParam String userId) {
        try {
            // VpcService에서 직접 호출
            vpcService.createVpc(vpcRequest, userId);
            return responseService.getSuccessResult();
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("VPC creation failed: " + e.getMessage());
        }
    }

    /**
     * VPC 삭제 엔드포인트
     *
     * @param vpcName   VPC 이름 (요청 파라미터로 전달)
     * @param userId    사용자 ID (요청 파라미터로 전달)
     * @return 삭제 성공 메시지 또는 오류 메시지
     */
    @DeleteMapping("/delete")
    public CommonResult deleteVpc(
            @RequestParam String vpcName,
            @RequestParam String userId) {
        try {
            // VpcService에서 직접 호출
            vpcService.deleteVpc(vpcName, userId);
            return responseService.getSuccessResult();
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("VPC deletion failed: " + e.getMessage());
        }
    }

    /**
     * VPC 네트워크 조회 API
     *
     * @param projectId GCP 프로젝트 ID
     * @return GCP 프로젝트에서 VPC 목록 및 상세 정보 반환
     */
    @GetMapping("/describe")
    public ResponseEntity<?> listVpcs(@RequestParam String projectId) {
        try {
            return gcpResourceService.listVpcsWithDetails(projectId); // List VPCs with the projectId
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to retrieve VPC details");
        }
    }
}
