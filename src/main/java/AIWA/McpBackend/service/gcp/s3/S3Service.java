package AIWA.McpBackend.service.gcp.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final String bucketName = "aiwa-terraform";

    /**
     * S3에서 특정 사용자 디렉토리 내 모든 파일 목록을 가져옵니다.
     *
     * @param userId 사용자 ID
     * @return 파일 키 목록
     */
    public List<String> listAllFiles(String userId) {
        String prefix = "users/" + userId + "/GCP/";
        List<String> fileKeys = new ArrayList<>();

        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .build();

        ListObjectsV2Response response;
        do {
            response = s3Client.listObjectsV2(request);
            response.contents().forEach(object -> {
                String key = object.key();
                if (!key.endsWith("/")) { // 폴더가 아닌 파일만 추가
                    fileKeys.add(key);
                }
            });
            request = request.toBuilder()
                    .continuationToken(response.nextContinuationToken())
                    .build();
        } while (response.isTruncated());

        return fileKeys;
    }

    public String downloadJsonFile(String userId) {
        String prefix = "users/" + userId + "/GCP/";
        String jsonFileName = null;

        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .build();

        ListObjectsV2Response response;
        do {
            response = s3Client.listObjectsV2(request);
            for (S3Object object : response.contents()) {
                String key = object.key();
                if (key.endsWith(".json") && !key.endsWith("/")) {
                    jsonFileName = key.substring(key.lastIndexOf("/") + 1);  // 파일 이름만 추출

                    // 파일 다운로드
                    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build();
                    ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);

                    // 파일 내용을 바이트 배열로 변환
                    try {
                        byte[] jsonFileData = s3Object.readAllBytes();
                    } catch (IOException e) {
                        // 예외 처리 코드
                        e.printStackTrace();
                    }


                    break;
                }
            }
            request = request.toBuilder()
                    .continuationToken(response.nextContinuationToken())
                    .build();
        } while (response.isTruncated() && jsonFileName == null);

        return jsonFileName;  // 파일 이름 반환
    }


    /**
     * S3에서 지정된 키에 해당하는 파일을 다운로드하여 문자열로 반환합니다.
     *
     * @param s3Key S3 객체 키
     * @return 파일 내용
     * @throws IOException 파일을 가져오는 중 오류가 발생한 경우
     */
    public String getFileContent(String s3Key) throws IOException {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(s3Client.getObject(getObjectRequest), StandardCharsets.UTF_8))) {
            StringBuilder contentBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
            return contentBuilder.toString();
        } catch (IOException e) {
            throw new IOException("S3에서 파일을 가져오는 중 오류 발생: " + s3Key, e);
        }
    }

    /**
     * S3에 파일 내용을 업로드합니다.
     *
     * @param s3Key   S3 객체 키
     * @param content 업로드할 내용
     */
    public void uploadFileContent(String s3Key, String content) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

        s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromString(content));
    }

    /**
     * S3에서 지정된 키에 해당하는 파일을 삭제합니다.
     *
     * @param s3Key S3 객체 키
     */
    public void deleteFile(String s3Key) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }

    public String getBucketName() {
        return bucketName;
    }
}
