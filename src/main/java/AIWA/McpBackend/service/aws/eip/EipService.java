package AIWA.McpBackend.service.aws.eip;

import AIWA.McpBackend.service.aws.s3.S3Service;
import AIWA.McpBackend.service.terraform.TerraformService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EipService {

    private final S3Service s3Service;
    private final TerraformService terraformService;

    public void createEip(String eipId,String userId) throws Exception {
        // 1. 새로운 EIP .tf 파일 생성
        String eipTfContent = String.format("""
            resource "aws_eip" "eip_%s" {
              vpc      = true
            }
            """, eipId);

        // 2. EIP .tf 파일 이름 설정 (예: eip_instanceId.tf)
        String eipTfFileName = String.format("eip_%s.tf", eipId);

        // 3. S3에 새로운 EIP .tf 파일 업로드
        String s3Key = "users/" + userId + "/" + eipTfFileName;
        s3Service.uploadFileContent(s3Key, eipTfContent);

        // 4. Terraform 실행 요청
        terraformService.executeTerraform(userId);
    }

    public void deleteEip(String userId, String eipId) throws Exception {
        // 1. 삭제할 EIP .tf 파일 이름 설정 (예: eip_instanceId.tf)
        String eipTfFileName = String.format("eip_%s.tf", eipId);

        // 2. S3에서 EIP .tf 파일 삭제
        String s3Key = "users/" + userId + "/" + eipTfFileName;
        s3Service.deleteFile(s3Key);

        // 3. Terraform 실행 요청
        terraformService.executeTerraform(userId);
    }
}