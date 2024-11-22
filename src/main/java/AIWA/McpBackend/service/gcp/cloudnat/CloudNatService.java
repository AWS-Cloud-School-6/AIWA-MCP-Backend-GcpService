package AIWA.McpBackend.service.gcp.cloudnat;

import AIWA.McpBackend.controller.api.dto.cloudnat.CloudNatRequestDto;
import AIWA.McpBackend.service.terraform.TerraformService;
import AIWA.McpBackend.service.gcp.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CloudNatService {

    private final S3Service s3Service;
    private final TerraformService terraformService;

    public void createCloudNat(CloudNatRequestDto cloudNatRequest, String userId) throws Exception {
        // 1. GCP Cloud NAT .tf 파일 생성
        String cloudNatTfContent = String.format("""
        resource "google_compute_router" "%s" {
          name    = "%s"
          region  = "%s"
          network = "%s"
        }

        resource "google_compute_router_nat" "%s" {
          name                               = "%s"
          router                             = google_compute_router.%s.name
          region                             = "%s"
          nat_ip_allocate_option             = "AUTO_ONLY"
          source_subnetwork_ip_ranges_to_nat = "ALL_SUBNETWORKS_ALL_IP_RANGES"

          log_config {
            enable   = true
            filter   = "ALL"  // 필터를 "ALL"로 설정
          }

          // 의존성 명시: google_compute_router가 생성된 후에 nat을 생성하도록 설정
          depends_on = [google_compute_router.%s]
        }
        """, cloudNatRequest.getRouterName(), cloudNatRequest.getRouterName(),
                cloudNatRequest.getRegion(), cloudNatRequest.getNetwork(),
                cloudNatRequest.getNatName(), cloudNatRequest.getNatName(),
                cloudNatRequest.getRouterName(), cloudNatRequest.getRegion(),
                cloudNatRequest.getRouterName());  // depends_on 추가

        // 2. Cloud NAT .tf 파일 이름 설정 (예: cloud_nat_myNat.tf)
        String cloudNatTfFileName = String.format("cloud_nat_%s.tf", cloudNatRequest.getNatName());

        // 3. 콘솔에 내용 출력 (디버깅 용도)
        System.out.println(cloudNatTfContent);

        // 4. GCS에 새로운 Cloud NAT .tf 파일 업로드
        String gcsKey = "users/" + userId + "/GCP/" + cloudNatTfFileName;
        s3Service.uploadFileContent(gcsKey, cloudNatTfContent);

        // 5. Terraform 실행 요청
        try {
            terraformService.executeTerraform(userId);
        } catch (Exception e) {
            System.err.println("Terraform 실행 중 오류 발생: " + e.getMessage());
            throw new Exception("Terraform 실행 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * NAT Gateway를 삭제합니다.
     *
     * @param cloudNatName NAT Gateway 이름
     * @param userId         사용자 ID
     * @throws Exception NAT Gateway 삭제 중 발생한 예외
     */
    public void deleteCloudNat(String cloudNatName, String userId) throws Exception {
        // 1. 삭제하려는 NAT Gateway .tf 파일 이름 설정 (예: nat_gateway_myCloudNat.tf)
        String cloudNatTfFileName = String.format("cloud_nat_%s.tf", cloudNatName);

        // 2. S3에서 해당 NAT Gateway .tf 파일 삭제
        String s3Key = "users/" + userId + "/GCP/" + cloudNatTfFileName;
        s3Service.deleteFile(s3Key);

        // 3. Terraform 실행 요청
        try {
            terraformService.executeTerraform(userId);
        } catch (Exception e) {
            System.err.println("Terraform 실행 중 오류 발생: " + e.getMessage());
            throw new Exception("Terraform 실행 중 오류가 발생했습니다.", e);
        }
    }
}
