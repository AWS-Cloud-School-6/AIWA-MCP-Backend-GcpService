package AIWA.McpBackend.service.aws.vpc;

import AIWA.McpBackend.controller.api.dto.vpc.VpcRequestDto;
import AIWA.McpBackend.service.aws.s3.S3Service;
import AIWA.McpBackend.service.terraform.TerraformService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VpcService {

    private final S3Service s3Service;

    private final TerraformService terraformService;
    /**
     * VPC를 생성합니다.
     *
     * @param vpcRequest VPC 생성 요청 DTO
     * @param userId     사용자 ID
     * @throws Exception VPC 생성 중 발생한 예외
     */

    public void createVpc(VpcRequestDto vpcRequest, String userId) throws Exception {
        // 1. 새로운 VPC .tf 파일 생성
        String vpcTfContent = String.format("""
                resource "aws_vpc" "%s" {
                  cidr_block = "%s"
                  tags = {
                    Name = "%s"
                  }
                }
                """, vpcRequest.getVpcName(), vpcRequest.getCidrBlock(), vpcRequest.getVpcName());

        // 2. VPC .tf 파일 이름 설정 (예: vpc_myVPC.tf)
        String vpcTfFileName = String.format("vpc_%s.tf", vpcRequest.getVpcName());

        // 3. 콘솔에 내용 출력 (디버깅 용도)
        System.out.println(vpcTfContent);

        // 4. S3에 새로운 VPC .tf 파일 업로드
        String s3Key = "users/" + userId + "/" + vpcTfFileName;
        s3Service.uploadFileContent(s3Key, vpcTfContent);

        // 5. Terraform 실행 요청
        terraformService.executeTerraform(userId);
    }

    /**
     * VPC를 삭제합니다.
     *
     * @param vpcName VPC 이름
     * @param userId  사용자 ID
     * @throws Exception VPC 삭제 중 발생한 예외
     */
    public void deleteVpc(String vpcName, String userId) throws Exception {
        // 1. 삭제하려는 VPC .tf 파일 이름 설정 (예: vpc_myVPC.tf)
        String vpcTfFileName = String.format("vpc_%s.tf", vpcName);

        // 2. S3에서 해당 VPC .tf 파일 삭제
        String s3Key = "users/" + userId + "/" + vpcTfFileName;
        s3Service.deleteFile(s3Key);

        // 3. Terraform 실행 요청
        terraformService.executeTerraform(userId);
    }

}
