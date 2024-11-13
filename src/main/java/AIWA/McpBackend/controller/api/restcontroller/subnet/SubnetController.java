package AIWA.McpBackend.controller.api.restcontroller.subnet;

import AIWA.McpBackend.controller.api.dto.response.ListResult;
import AIWA.McpBackend.controller.api.dto.subnet.SubnetResponseDto;
import AIWA.McpBackend.service.gcp.GcpResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gcp/api/subnet")
@RequiredArgsConstructor
public class SubnetController {

    @Autowired
    private final GcpResourceService gcpResourceService;

    // 서브넷 정보를 가져오는 API 엔드포인트
    @GetMapping("/describe")
    public ResponseEntity<?> listSubnets(@RequestParam String projectId) {
        try {
            // 서브넷 리스트 조회
            ListResult<SubnetResponseDto> subnetList = gcpResourceService.listSubnets(projectId);

            // 성공적으로 서브넷 정보를 조회했다면 200 OK 상태로 반환
            return ResponseEntity.ok(subnetList);

        } catch (Exception e) {
            // 예외 발생 시 500 Internal Server Error 상태 코드와 에러 메시지 반환
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error occurred while retrieving subnets: " + e.getMessage());
        }
    }
}