package AIWA.McpBackend.service.aws.ec2;

import AIWA.McpBackend.controller.api.dto.ec2.Ec2RequestDto;
import AIWA.McpBackend.service.terraform.TerraformService;
import AIWA.McpBackend.service.aws.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Ec2Service {

    private final S3Service s3Service;
    private final TerraformService terraformService;

    /**
     * EC2 인스턴스를 생성합니다.
     *
     * @param instanceRequest EC2 인스턴스 생성 요청 DTO
     * @param userId          사용자 ID
     * @throws Exception EC2 인스턴스 생성 중 발생한 예외
     */
    public void createEC2Instance(Ec2RequestDto instanceRequest, String userId) throws Exception {
        // 1. EC2 인스턴스 .tf 파일 내용 생성
        String ec2TfContent = String.format("""
            resource "aws_instance" "%s" {
              ami           = "%s"
              instance_type = "%s"
              subnet_id     = aws_subnet.%s.id
              vpc_security_group_ids = [aws_security_group.%s.id]
              key_name = "%s"
              tags = {
                Name = "%s"
              }
            }
            """,
                instanceRequest.getInstanceName(),
                instanceRequest.getAmiId(),
                instanceRequest.getInstanceType(),
                instanceRequest.getSubnetName(),
                instanceRequest.getSecurityGroupName(),
                instanceRequest.getKeyPairName(),
                instanceRequest.getInstanceName());

        // 2. EC2 인스턴스 .tf 파일 이름 설정 (예: ec2_myInstance.tf)
        String ec2TfFileName = String.format("ec2_%s.tf", instanceRequest.getInstanceName());

        // 3. S3에 새로운 EC2 인스턴스 .tf 파일 업로드
        String s3Key = "users/" + userId + "/" + ec2TfFileName;
        s3Service.uploadFileContent(s3Key, ec2TfContent);

        // 4. Terraform 실행 요청
        terraformService.executeTerraform(userId);
    }

    /**
     * EC2 인스턴스를 삭제합니다.
     *
     * @param instanceName EC2 인스턴스 이름
     * @param userId       사용자 ID
     * @throws Exception EC2 인스턴스 삭제 중 발생한 예외
     */
    public void deleteEC2Instance(String instanceName, String userId) throws Exception {
        // 1. 삭제하려는 EC2 인스턴스 .tf 파일 이름 설정 (예: ec2_myInstance.tf)
        String ec2TfFileName = String.format("ec2_%s.tf", instanceName);

        // 2. S3에서 해당 EC2 인스턴스 .tf 파일 삭제
        String s3Key = "users/" + userId + "/" + ec2TfFileName;
        s3Service.deleteFile(s3Key);

        // 3. Terraform 실행 요청
        terraformService.executeTerraform(userId);
    }
}