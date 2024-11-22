package AIWA.McpBackend.service.gcp.cloudmonitoring;

import AIWA.McpBackend.controller.api.dto.cloudmonitoring.CloudMonitoringRequestDto;
import AIWA.McpBackend.service.gcp.s3.S3Service;
import AIWA.McpBackend.service.terraform.TerraformService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CloudMonitoringService {

    private final S3Service s3Service;
    private final TerraformService terraformService;

    /**
     * CloudWatch 알람을 생성합니다.
     *
     * @param alarmRequest CloudWatch 알람 요청 DTO
     * @param userId       사용자 ID
     * @throws Exception CloudWatch 알람 생성 중 발생한 예외
     */
    public void createCloudMonitoringAlarm(CloudMonitoringRequestDto alarmRequest, String userId) throws Exception {
        // 1. CloudWatch 알람 .tf 파일 생성
        String alarmTfContent = String.format("""
                resource "google_monitoring_dashboard" "comprehensive_dashboard" {
                  dashboard_json = <<JSON
                  {
                    "displayName": "%s",
                    "gridLayout": {
                      "columns": %d,
                      "widgets": [
                        {
                          "title": "%s",
                          "xyChart": {
                            "dataSets": [
                              {
                                "timeSeriesQuery": {
                                  "timeSeriesFilter": {
                                    "filter": "metric.type=\\"compute.googleapis.com/instance/cpu/utilization\\"",
                                    "aggregation": {
                                      "alignmentPeriod": "60s",
                                      "perSeriesAligner": "ALIGN_MEAN"
                                    }
                                  }
                                }
                              }
                            ]
                          }
                        },
                        {
                          "title": "%s",
                          "xyChart": {
                            "dataSets": [
                              {
                                "timeSeriesQuery": {
                                  "timeSeriesFilter": {
                                    "filter": "metric.type=\\"compute.googleapis.com/instance/memory/usage\\"",
                                    "aggregation": {
                                      "alignmentPeriod": "60s",
                                      "perSeriesAligner": "ALIGN_MEAN"
                                    }
                                  }
                                }
                              }
                            ]
                          }
                        },
                        {
                          "title": "%s",
                          "xyChart": {
                            "dataSets": [
                              {
                                "timeSeriesQuery": {
                                  "timeSeriesFilter": {
                                    "filter": "metric.type=\\"kubernetes.io/node/cpu/usage_rate\\"",
                                    "aggregation": {
                                      "alignmentPeriod": "60s",
                                      "perSeriesAligner": "ALIGN_MEAN"
                                    }
                                  }
                                }
                              }
                            ]
                          }
                        },
                        {
                          "title": "%s",
                          "xyChart": {
                            "dataSets": [
                              {
                                "timeSeriesQuery": {
                                  "timeSeriesFilter": {
                                    "filter": "metric.type=\\"cloudsql.googleapis.com/database/cpu/utilization\\"",
                                    "aggregation": {
                                      "alignmentPeriod": "60s",
                                      "perSeriesAligner": "ALIGN_MEAN"
                                    }
                                  }
                                }
                              }
                            ]
                          }
                        },
                        {
                          "title": "%s",
                          "xyChart": {
                            "dataSets": [
                              {
                                "timeSeriesQuery": {
                                  "timeSeriesFilter": {
                                    "filter": "metric.type=\\"cloudsql.googleapis.com/database/disk/bytes_used\\"",
                                    "aggregation": {
                                      "alignmentPeriod": "60s",
                                      "perSeriesAligner": "ALIGN_MEAN"
                                    }
                                  }
                                }
                              }
                            ]
                          }
                        },
                        {
                          "title": "%s",
                          "xyChart": {
                            "dataSets": [
                              {
                                "timeSeriesQuery": {
                                  "timeSeriesFilter": {
                                    "filter": "metric.type=\\"storage.googleapis.com/storage/object/request_count\\"",
                                    "aggregation": {
                                      "alignmentPeriod": "60s",
                                      "perSeriesAligner": "ALIGN_RATE"
                                    }
                                  }
                                }
                              }
                            ]
                          }
                        },
                        {
                          "title": "%s",
                          "xyChart": {
                            "dataSets": [
                              {
                                "timeSeriesQuery": {
                                  "timeSeriesFilter": {
                                    "filter": "metric.type=\\"storage.googleapis.com/storage/total_bytes\\"",
                                    "aggregation": {
                                      "alignmentPeriod": "60s",
                                      "perSeriesAligner": "ALIGN_MEAN"
                                    }
                                  }
                                }
                              }
                            ]
                          }
                        }
                      ]
                    }
                  }
                  JSON
                }
                """,
                alarmRequest.getDisplayName(),
                alarmRequest.getColumns(),
                alarmRequest.getComputeCpuChartTitle(),
                alarmRequest.getComputeMemoryChartTitle(),
                alarmRequest.getGkeCpuChartTitle(),
                alarmRequest.getCloudSqlCpuChartTitle(),
                alarmRequest.getCloudSqlDiskChartTitle(),
                alarmRequest.getCloudStorageRequestCountChartTitle(),
                alarmRequest.getCloudStorageTotalBytesChartTitle());

        // 2. CloudWatch 알람 .tf 파일 이름 설정 (예: cloudwatch_alarm_myAlarm.tf)
        String alarmTfFileName = String.format("cloudmonitoring_alarm_%s.tf", alarmRequest.getDisplayName());

        // 3. S3에 새로운 CloudWatch 알람 .tf 파일 업로드
        String s3Key = "users/" + userId + "/GCP/" + alarmTfFileName;
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
    public void deleteCloudMonitoringAlarm(String alarmName, String userId) throws Exception {
        // 1. 삭제하려는 CloudWatch 알람 .tf 파일 이름 설정 (예: cloudwatch_alarm_myAlarm.tf)
        String alarmTfFileName = String.format("cloudmonitoring_alarm_%s.tf", alarmName);

        // 2. S3에서 해당 CloudWatch 알람 .tf 파일 삭제
        String s3Key = "users/" + userId + "/GCP/" + alarmTfFileName;
        s3Service.deleteFile(s3Key);

        // 3. Terraform 실행 요청
        terraformService.executeTerraform(userId);
    }
}