package AIWA.McpBackend.service.aws.cloudwatch;

import AIWA.McpBackend.controller.api.dto.cloudwatch.CloudWatchAlarmRequestDto;
import AIWA.McpBackend.service.aws.s3.S3Service;
import AIWA.McpBackend.service.terraform.TerraformService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CloudWatchService {

    private final S3Service s3Service;
    private final TerraformService terraformService;

    /**
     * CloudWatch 알람을 생성합니다.
     *
     * @param alarmRequest CloudWatch 알람 요청 DTO
     * @param userId       사용자 ID
     * @throws Exception CloudWatch 알람 생성 중 발생한 예외
     */
    public void createCloudWatchAlarm(CloudWatchAlarmRequestDto alarmRequest, String userId) throws Exception {
        // 1. CloudWatch 알람 .tf 파일 생성
        String alarmTfContent = String.format("""
                resource "aws_cloudwatch_metric_alarm" "%s" {
                  alarm_name          = "%s"
                  comparison_operator = "%s"
                  evaluation_periods  = %d
                  metric_name         = "%s"
                  namespace           = "%s"
                  period              = %d
                  statistic           = "%s"
                  threshold           = %.2f
                  alarm_actions       = []
                  dimensions = {
                    InstanceId = "%s"
                  }
                }
                """,
                alarmRequest.getAlarmName(),
                alarmRequest.getAlarmName(),
                alarmRequest.getComparisonOperator(),
                alarmRequest.getEvaluationPeriods(),
                alarmRequest.getMetricName(),
                alarmRequest.getNamespace(),
                Integer.parseInt(alarmRequest.getPeriod()),
                alarmRequest.getStatistic(),
                alarmRequest.getThreshold(),
                alarmRequest.getInstanceId());

        // 2. CloudWatch 알람 .tf 파일 이름 설정 (예: cloudwatch_alarm_myAlarm.tf)
        String alarmTfFileName = String.format("cloudwatch_alarm_%s.tf", alarmRequest.getAlarmName());

        // 3. S3에 새로운 CloudWatch 알람 .tf 파일 업로드
        String s3Key = "users/" + userId + "/" + alarmTfFileName;
        s3Service.uploadFileContent(s3Key, alarmTfContent);

        // 4. Terraform 실행 요청
        terraformService.executeTerraform(userId);
    }

    /**
     * CloudWatch 알람을 삭제합니다.
     *
     * @param alarmName 알람 이름
     * @param userId    사용자 ID
     * @throws Exception CloudWatch 알람 삭제 중 발생한 예외
     */
    public void deleteCloudWatchAlarm(String alarmName, String userId) throws Exception {
        // 1. 삭제하려는 CloudWatch 알람 .tf 파일 이름 설정 (예: cloudwatch_alarm_myAlarm.tf)
        String alarmTfFileName = String.format("cloudwatch_alarm_%s.tf", alarmName);

        // 2. S3에서 해당 CloudWatch 알람 .tf 파일 삭제
        String s3Key = "users/" + userId + "/" + alarmTfFileName;
        s3Service.deleteFile(s3Key);

        // 3. Terraform 실행 요청
        terraformService.executeTerraform(userId);
    }
}