package AIWA.McpBackend.controller.api.dto.cloudwatch;

import lombok.Data;

@Data
public class CloudWatchAlarmRequestDto {
    private String alarmName;          // 알람 이름
    private String metricName;         // 모니터링할 메트릭 이름 (예: CPUUtilization)
    private String namespace;          // 메트릭 네임스페이스 (예: AWS/EC2)
    private String statistic;          // 통계 유형 (예: Average, Sum)
    private String comparisonOperator; // 비교 연산자 (예: GreaterThanThreshold)
    private double threshold;          // 임계값
    private int evaluationPeriods;     // 평가 주기 (메트릭을 평가할 기간)
    private String period;             // 평가할 주기 (예: 300초)
    private String instanceId;         // 알람을 적용할 EC2 인스턴스 ID
}