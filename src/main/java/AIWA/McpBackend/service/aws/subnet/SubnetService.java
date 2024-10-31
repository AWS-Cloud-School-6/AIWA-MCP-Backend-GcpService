package AIWA.McpBackend.service.aws.subnet;


import AIWA.McpBackend.controller.api.dto.subnet.SubnetRequestDto;
import AIWA.McpBackend.service.aws.s3.S3Service;
import AIWA.McpBackend.service.terraform.TerraformService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubnetService {

    private final S3Service s3Service;
    private final TerraformService terraformService;

    /**
     * Subnet을 생성합니다.
     *
     * @param subnetRequest Subnet 생성 요청 DTO
     * @param userId        사용자 ID
     * @throws Exception Subnet 생성 중 발생한 예외
     */
    public void createSubnet(SubnetRequestDto subnetRequest, String userId) throws Exception {
        // 1. 새로운 Subnet .tf 파일 생성
        String subnetTfContent = String.format("""
                resource "aws_subnet" "%s" {
                  vpc_id = aws_vpc.%s.id
                  cidr_block = "%s"
                  availability_zone = "%s"
                  tags = {
                    Name = "%s"
                  }
                }
                """,
                subnetRequest.getSubnetName(),
                subnetRequest.getVpcName(),
                subnetRequest.getCidrBlock(),
                subnetRequest.getAvailabilityZone(),
                subnetRequest.getSubnetName());

        // 2. Subnet .tf 파일 이름 설정 (예: subnet_mySubnet.tf)
        String subnetTfFileName = String.format("subnet_%s.tf", subnetRequest.getSubnetName());

        // 3. 콘솔에 내용 출력 (디버깅 용도)
        System.out.println(subnetTfContent);

        // 4. S3에 새로운 Subnet .tf 파일 업로드
        String s3Key = "users/" + userId + "/" + subnetTfFileName;
        s3Service.uploadFileContent(s3Key, subnetTfContent);

        // 5. Terraform 실행 요청
        terraformService.executeTerraform(userId);
    }

    /**
     * Subnet을 삭제합니다.
     *
     * @param subnetName Subnet 이름
     * @param userId     사용자 ID
     * @throws Exception Subnet 삭제 중 발생한 예외
     */
    public void deleteSubnet(String subnetName, String userId) throws Exception {
        // 1. 삭제하려는 Subnet .tf 파일 이름 설정 (예: subnet_mySubnet.tf)
        String subnetTfFileName = String.format("subnet_%s.tf", subnetName);

        // 2. S3에서 해당 Subnet .tf 파일 삭제
        String s3Key = "users/" + userId + "/" + subnetTfFileName;
        s3Service.deleteFile(s3Key);

        // 3. Terraform 실행 요청
        terraformService.executeTerraform(userId);
    }
}
