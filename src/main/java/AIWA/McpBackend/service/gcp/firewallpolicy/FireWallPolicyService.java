package AIWA.McpBackend.service.gcp.firewallpolicy;

import AIWA.McpBackend.controller.api.dto.firewallpolicy.FirewallPolicyRequestDto;
import AIWA.McpBackend.service.gcp.s3.S3Service;
import AIWA.McpBackend.service.terraform.TerraformService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FireWallPolicyService {

    private final S3Service s3Service;
    private final TerraformService terraformService;

    /**
     * GCP FirewallPolicy 생성
     *
     * @param firewallPolicyRequest 방화벽 정책 생성 요청 DTO
     * @param userId               사용자 ID
     * @throws Exception FirewallPolicy 생성 중 발생한 예외
     */
    public void createFirewallPolicy(FirewallPolicyRequestDto firewallPolicyRequest, String userId, String projectId) throws Exception {
        // 1. FirewallPolicy .tf 파일 생성
        String firewallPolicyTfContent = String.format("""
                resource "google_compute_firewall" "%s" {
                  name          = "%s"
                  network       = "projects/%s/global/networks/%s"
                  direction     = "%s"
                  source_ranges = ["%s"]
                  target_tags   = ["%s"]
                  allow {
                    protocol = "%s"
                    ports    = ["%d"]
                  }
                }
                """,
                firewallPolicyRequest.getPolicyName(),
                firewallPolicyRequest.getPolicyName(),
                projectId,                         // 사용자 ID를 통해 프로젝트 ID 사용
                firewallPolicyRequest.getNetworkName(),
                firewallPolicyRequest.getDirection(),
                firewallPolicyRequest.getSourceRange(),
                firewallPolicyRequest.getTargetTag(),
                firewallPolicyRequest.getProtocol(),
                firewallPolicyRequest.getPort()
        );

        // 2. FirewallPolicy .tf 파일 이름 설정 (예: firewall_myPolicy.tf)
        String firewallPolicyTfFileName = String.format("firewall_%s.tf", firewallPolicyRequest.getPolicyName());

        // 3. GCS에 새로운 FirewallPolicy .tf 파일 업로드
        String gcsKey = "users/" + userId + "/GCP/" + firewallPolicyTfFileName;
        s3Service.uploadFileContent(gcsKey, firewallPolicyTfContent);

        // 4. Terraform 실행 요청
        terraformService.executeTerraform(userId);
    }

    /**
     * GCP FirewallPolicy 삭제
     *
     * @param policyName 방화벽 정책 이름
     * @param userId     사용자 ID
     * @throws Exception FirewallPolicy 삭제 중 발생한 예외
     */
    public void deleteFirewallPolicy(String policyName, String userId) throws Exception {
        // 1. 삭제할 FirewallPolicy .tf 파일 이름 설정 (예: firewall_myPolicy.tf)
        String firewallPolicyTfFileName = String.format("firewall_%s.tf", policyName);

        // 2. GCS에서 해당 FirewallPolicy .tf 파일 삭제
        String gcsKey = "users/" + userId + "/GCP/" + firewallPolicyTfFileName;
        s3Service.deleteFile(gcsKey);

        // 3. Terraform 실행 요청
        terraformService.executeTerraform(userId);
    }
}
