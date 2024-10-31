package AIWA.McpBackend.service.aws.internetgateway;

import AIWA.McpBackend.controller.api.dto.internetgateway.InternetGatewayRequestDto;
import AIWA.McpBackend.service.terraform.TerraformService;
import AIWA.McpBackend.service.aws.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InternetGatewayService {

    private final S3Service s3Service;
    private final TerraformService terraformService;

    /**
     * Internet Gateway를 생성합니다.
     *
     * @param igwRequest Internet Gateway 생성 요청 DTO
     * @param userId     사용자 ID
     * @throws Exception Internet Gateway 생성 중 발생한 예외
     */
    public void createInternetGateway(InternetGatewayRequestDto igwRequest, String userId) throws Exception {
        // 1. 새로운 Internet Gateway .tf 파일 생성
        String igwTfContent = String.format("""
                resource "aws_internet_gateway" "%s" {
                  vpc_id = aws_vpc.%s.id
                  tags = {
                    Name = "%s"
                  }
                }
                """, igwRequest.getIgwName(), igwRequest.getVpcName(), igwRequest.getIgwName());

        // 2. Internet Gateway .tf 파일 이름 설정 (예: igw_myInternetGateway.tf)
        String igwTfFileName = String.format("igw_%s.tf", igwRequest.getIgwName());

        // 3. S3에 새로운 Internet Gateway .tf 파일 업로드
        String s3Key = "users/" + userId + "/" + igwTfFileName;
        s3Service.uploadFileContent(s3Key, igwTfContent);

        // 4. Terraform 실행 요청
        terraformService.executeTerraform(userId);
    }

    /**
     * Internet Gateway를 삭제합니다.
     *
     * @param igwName Internet Gateway 이름
     * @param userId  사용자 ID
     * @throws Exception Internet Gateway 삭제 중 발생한 예외
     */
    public void deleteInternetGateway(String igwName, String userId) throws Exception {
        // 1. 삭제하려는 Internet Gateway .tf 파일 이름 설정 (예: igw_myInternetGateway.tf)
        String igwTfFileName = String.format("igw_%s.tf", igwName);

        // 2. S3에서 해당 Internet Gateway .tf 파일 삭제
        String s3Key = "users/" + userId + "/" + igwTfFileName;
        s3Service.deleteFile(s3Key);

        // 3. Terraform 실행 요청
        terraformService.executeTerraform(userId);
    }
}