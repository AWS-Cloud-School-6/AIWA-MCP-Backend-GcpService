package AIWA.McpBackend.service.gcp.subnet;

import AIWA.McpBackend.controller.api.dto.subnet.SubnetRequestDto;
import AIWA.McpBackend.service.gcp.s3.S3Service;
import AIWA.McpBackend.service.terraform.TerraformService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubnetService {

    private final S3Service s3Service;
    private final TerraformService terraformService;

    /**
     * GCP Subnet 생성
     *
     * @param subnetRequest Subnet 생성 요청 DTO
     * @param userId        사용자 ID
     * @throws Exception Subnet 생성 중 발생한 예외
     */
    public void createSubnet(SubnetRequestDto subnetRequest, String userId) throws Exception {
        // 1. Subnet .tf 파일 생성
        String subnetTfContent = String.format("""
                resource "google_compute_subnetwork" "%s" {
                  name          = "%s"
                  ip_cidr_range = "%s"
                  network       = "projects/global/networks/%s"
                  region        = "asia-northeast3"
                }
                """,
                subnetRequest.getSubnetName(),
                subnetRequest.getSubnetName(),
                subnetRequest.getIpCidrRange(),
                subnetRequest.getVpcName());

        // 2. Subnet .tf 파일 이름 설정 (예: subnet_mySubnet.tf)
        String subnetTfFileName = String.format("subnet_%s.tf", subnetRequest.getSubnetName());

        // 3. GCS에 새로운 Subnet .tf 파일 업로드
        String gcsKey = "users/" + userId + "/GCP/" + subnetTfFileName;
        s3Service.uploadFileContent(gcsKey, subnetTfContent);

        // 4. Terraform 실행 요청
        terraformService.executeTerraform(userId);
    }

    /**
     * GCP Subnet 삭제
     *
     * @param subnetName Subnet 이름
     * @param userId     사용자 ID
     * @throws Exception Subnet 삭제 중 발생한 예외
     */
    public void deleteSubnet(String subnetName, String userId) throws Exception {
        // 1. 삭제하려는 Subnet .tf 파일 이름 설정 (예: subnet_mySubnet.tf)
        String subnetTfFileName = String.format("subnet_%s.tf", subnetName);

        // 2. GCS에서 해당 Subnet .tf 파일 삭제
        String gcsKey = "users/" + userId + "/GCP/" + subnetTfFileName;
        s3Service.deleteFile(gcsKey);

        // 3. Terraform 실행 요청
        terraformService.executeTerraform(userId);
    }
}
