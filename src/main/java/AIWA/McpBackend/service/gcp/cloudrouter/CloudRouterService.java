package AIWA.McpBackend.service.gcp.cloudrouter;

import AIWA.McpBackend.controller.api.dto.cloudrouter.CloudRouterRequestDto;
import AIWA.McpBackend.controller.api.dto.internetgateway.InternetGatewayRequestDto;
import AIWA.McpBackend.service.terraform.TerraformService;
import AIWA.McpBackend.service.gcp.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CloudRouterService {

    private final S3Service s3Service;
    private final TerraformService terraformService;

    /**
     * Internet Gateway를 생성합니다.
     *
     * @param cloudRouterRequest Internet Gateway 생성 요청 DTO
     * @param userId     사용자 ID
     * @throws Exception Internet Gateway 생성 중 발생한 예외
     */
    public void createCloudRouter(CloudRouterRequestDto cloudRouterRequest, String userId) throws Exception {
        // 1. 새로운 Cloud Router .tf 파일 생성
        String cloudRouterTfContent = String.format("""
            resource "google_compute_router" "%s" {
              name    = "%s"
              region  = "%s"
              network = "%s"
              description = "%s"
            }
            """, cloudRouterRequest.getRouterName(), cloudRouterRequest.getRouterName(),
                cloudRouterRequest.getRegion(), cloudRouterRequest.getNetwork(), cloudRouterRequest.getDescription());

        // 2. Cloud Router .tf 파일 이름 설정 (예: cloud_router_myRouter.tf)
        String cloudRouterTfFileName = String.format("cloud_router_%s.tf", cloudRouterRequest.getRouterName());

        // 3. 콘솔에 내용 출력 (디버깅 용도)
        System.out.println(cloudRouterTfContent);

        // 4. GCS에 새로운 Cloud Router .tf 파일 업로드
        String gcsKey = "users/" + userId + "/GCP/" + cloudRouterTfFileName;
        s3Service.uploadFileContent(gcsKey, cloudRouterTfContent);

        // 5. Terraform 실행 요청
        terraformService.executeTerraform(userId);
    }

    public void deleteCloudRouter(String routerName, String userId) throws Exception {
        // 1. 삭제하려는 Cloud Router .tf 파일 이름 설정 (예: cloud_router_myRouter.tf)
        String cloudRouterTfFileName = String.format("cloud_router_%s.tf", routerName);

        // 2. GCS에서 해당 Cloud Router .tf 파일 삭제
        String gcsKey = "users/" + userId + "/GCP/" + cloudRouterTfFileName;
        s3Service.deleteFile(gcsKey);

        // 3. Terraform 실행 요청
        terraformService.executeTerraform(userId);
    }
}