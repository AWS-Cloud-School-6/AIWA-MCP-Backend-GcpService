package AIWA.McpBackend.service.terraform;

import AIWA.McpBackend.service.gcp.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;  // HttpClient 5.x
import org.apache.hc.client5.http.config.RequestConfig;  // Correct import for RequestConfig 5.x
import org.apache.hc.core5.util.Timeout; // For setting timeout (5.x)
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TerraformService {
    private final S3Service s3Service;
    @Value("${s3.bucket.name}")
    private String bucketName;

    private static final int TIMEOUT = 600000;  // 타임아웃 10로 설정

    public void executeTerraform(String userId) throws Exception {
        String userPrefix = "users/" + userId + "/GCP/";
        List<String> fileKeys = s3Service.listAllFiles(userId);

        if (fileKeys.isEmpty()) {
            throw new Exception("S3에 Terraform 관련 파일이 없습니다: " + userPrefix);
        }

        // 원격에서 실행할 명령어 목록을 준비합니다.
        String remoteDir = "/home/" + userId + "/terraform"; // Pod 내 작업 디렉토리
        StringBuilder commands = new StringBuilder();
        commands.append("mkdir -p ").append(remoteDir).append(" && ");

        // S3에서 Terraform 파일 다운로드
        for (String key : fileKeys) {
            String fileName = key.substring(userPrefix.length());
            String downloadCommand = "aws s3 cp s3://" + bucketName + "/" + key + " " + remoteDir + "/" + fileName;
            commands.append(downloadCommand).append(" && ");
        }

        // Terraform 초기화 및 적용
        String initCommand = "cd " + remoteDir + " && terraform init";
        String applyCommand = "cd " + remoteDir + " && terraform apply -auto-approve";
        commands.append(initCommand).append(" && ").append(applyCommand);

        // Terraform 상태 파일 업로드 명령어
        String tfStateRemotePath = remoteDir + "/terraform.tfstate";
        String tfStateS3Key = userPrefix + "terraform.tfstate";
        String uploadCommand = "aws s3 cp " + tfStateRemotePath + " s3://" + bucketName + "/" + tfStateS3Key;
        commands.append(" && ").append(uploadCommand);

        // Terraform 파일 및 디렉토리 삭제
        String cleanUpCommand = "rm -rf " + remoteDir;  // 다운로드한 Terraform 파일 및 디렉토리 삭제
        commands.append(" && ").append(cleanUpCommand);


        // 명령어를 terraform-svc의 API로 전송하여 실행
        executeRemoteCommands(userId, commands.toString());
    }

    private void executeRemoteCommands(String userId, String commands) throws IOException {
        // terraform-svc의 외부 IP 및 포트 설정 (application.properties에서 설정된 값을 사용)
        String url = "http://" + "terraform-svc" + "/terraform/api/terraform/execute";

        // HttpClient의 타임아웃 설정 (밀리초 단위)
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(TIMEOUT))  // 연결 타임아웃 (밀리초)
                .setResponseTimeout(Timeout.ofMilliseconds(TIMEOUT)) // 읽기 타임아웃 (밀리초)
                .build();

        HttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();

        // RestTemplate에 HttpClient 적용
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        RestTemplate restTemplate = new RestTemplate(factory);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        // 요청 바디 생성 (userId와 명령어를 JSON 형식으로 전송)
        String requestBody = String.format("{\"userId\": \"%s\", \"commands\": \"%s\"}", userId, commands);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            // HTTP POST 요청을 통해 명령어 전송 및 실행 요청
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            // 응답 확인
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Terraform 작업이 성공적으로 실행되었습니다: " + response.getBody());
            } else {
                throw new IOException("Terraform 작업 실행 실패: " + response.getBody());
            }
        } catch (Exception e) {
            throw new IOException("Terraform 작업 요청 중 오류 발생: " + e.getMessage(), e);
        }
    }
}
