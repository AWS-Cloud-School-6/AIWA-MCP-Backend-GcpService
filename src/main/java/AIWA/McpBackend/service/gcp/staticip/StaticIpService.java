package AIWA.McpBackend.service.gcp.staticip;

import AIWA.McpBackend.controller.api.dto.staticip.StaticIpRequestDto;
import AIWA.McpBackend.service.gcp.s3.S3Service;
import AIWA.McpBackend.service.terraform.TerraformService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StaticIpService {

    private final S3Service s3Service;
    private final TerraformService terraformService;

    /**
     * GCP Static IP 생성
     *
     * @param staticIpRequest Static IP 생성 요청 DTO
     * @param userId          사용자 ID
     * @throws Exception Static IP 생성 중 발생한 예외
     */
    public void createStaticIp(StaticIpRequestDto staticIpRequest, String userId) throws Exception {
        // 1. Static IP .tf 파일 생성
        String staticIpTfContent = String.format("""
                resource "google_compute_address" "%s" {
                  name          = "%s"
                  region        = "%s"
                  address_type  = "%s"
                }
                """,
                staticIpRequest.getIpName(),
                staticIpRequest.getIpName(),
                staticIpRequest.getRegion(),
                staticIpRequest.getAddressType()
        );

        // 2. Static IP .tf 파일 이름 설정 (예: static-ip_myIp.tf)
        String staticIpTfFileName = String.format("static-ip_%s.tf", staticIpRequest.getIpName());

        // 3. GCS에 새로운 Static IP .tf 파일 업로드
        String gcsKey = "users/" + userId + "/GCP/" + staticIpTfFileName;
        s3Service.uploadFileContent(gcsKey, staticIpTfContent);

        // 4. Terraform 실행 요청
        terraformService.executeTerraform(userId);
    }

    /**
     * GCP Static IP 삭제
     *
     * @param ipName Static IP 이름
     * @param userId 사용자 ID
     * @throws Exception Static IP 삭제 중 발생한 예외
     */
    public void deleteStaticIp(String ipName, String userId) throws Exception {
        // 1. 삭제하려는 Static IP .tf 파일 이름 설정 (예: static-ip_myIp.tf)
        String staticIpTfFileName = String.format("static-ip_%s.tf", ipName);

        // 2. GCS에서 해당 Static IP .tf 파일 삭제
        String gcsKey = "users/" + userId + "/GCP/" + staticIpTfFileName;
        s3Service.deleteFile(gcsKey);

        // 3. Terraform 실행 요청
        terraformService.executeTerraform(userId);
    }
}
