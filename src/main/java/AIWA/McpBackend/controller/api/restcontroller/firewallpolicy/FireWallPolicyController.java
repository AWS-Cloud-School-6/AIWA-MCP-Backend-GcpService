package AIWA.McpBackend.controller.api.restcontroller.firewallpolicy;

import AIWA.McpBackend.controller.api.dto.firewallpolicy.FirewallPolicyRequestDto;
import AIWA.McpBackend.controller.api.dto.response.CommonResult;
import AIWA.McpBackend.service.gcp.firewallpolicy.FireWallPolicyService;
import AIWA.McpBackend.service.response.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gcp/api/firewallpolicy")
@RequiredArgsConstructor
public class FireWallPolicyController {

    private final FireWallPolicyService firewallPolicyService;
    private final ResponseService responseService;

    /**
     * GCP FirewallPolicy 생성 엔드포인트
     *
     * @param firewallPolicyRequest FirewallPolicy 생성 요청 DTO
     * @param userId               사용자 ID
     * @return 생성 성공 메시지 또는 오류 메시지
     */
    @PostMapping("/create")
    public CommonResult createFirewallPolicy(
            @RequestBody FirewallPolicyRequestDto firewallPolicyRequest,
            @RequestParam String userId) {
        try {
            // FirewallPolicyService에서 직접 호출하여 FirewallPolicy 생성
            firewallPolicyService.createFirewallPolicy(firewallPolicyRequest, userId);
            return responseService.getSuccessResult();
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("FirewallPolicy creation failed: " + e.getMessage());
        }
    }

    /**
     * GCP FirewallPolicy 삭제 엔드포인트
     *
     * @param policyName 방화벽 정책 이름
     * @param userId     사용자 ID
     * @return 삭제 성공 메시지 또는 오류 메시지
     */
    @DeleteMapping("/delete")
    public CommonResult deleteFirewallPolicy(
            @RequestParam String policyName,
            @RequestParam String userId) {
        try {
            // FirewallPolicyService에서 직접 호출하여 FirewallPolicy 삭제
            firewallPolicyService.deleteFirewallPolicy(policyName, userId);
            return responseService.getSuccessResult();
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("FirewallPolicy deletion failed: " + e.getMessage());
        }
    }
}
