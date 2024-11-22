package AIWA.McpBackend.controller.api.restcontroller.routetable;

import AIWA.McpBackend.controller.api.dto.routetable.RouteTableRequestDto;
import AIWA.McpBackend.controller.api.dto.routetable.RoutePolicyDto;  // 추가
import AIWA.McpBackend.controller.api.dto.response.CommonResult;
import AIWA.McpBackend.service.gcp.GcpResourceService;
import AIWA.McpBackend.service.gcp.routetable.RouteTableService;
import AIWA.McpBackend.service.gcp.routetable.RouteTableService; // 추가
import AIWA.McpBackend.service.response.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gcp/api/routetable")
@RequiredArgsConstructor
public class RouteTableController {

    private final RouteTableService routeTableService;

    private final GcpResourceService gcpResourceService;
    private final ResponseService responseService;

    /**
     * RouteTable 생성 엔드포인트
     *
     * @param routeRequest RouteTable 생성 요청 DTO
     * @param userId      사용자 ID
     * @return 생성 성공 메시지 또는 오류 메시지
     */
    @PostMapping("/create")
    public CommonResult createRouteTable(
            @RequestBody RouteTableRequestDto routeRequest,
            @RequestParam String userId) {
        try {
            // RouteTableService에서 직접 호출하여 라우트 테이블 생성
            routeTableService.createRouteTable(routeRequest, userId);
            return responseService.getSuccessResult();
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("RouteTable creation failed: " + e.getMessage());
        }
    }

    /**
     * RouteTable 삭제 엔드포인트
     *
     * @param routeName RouteTable 이름 (요청 파라미터로 전달)
     * @param userId   사용자 ID
     * @return 삭제 성공 메시지 또는 오류 메시지
     */
    @DeleteMapping("/delete")
    public CommonResult deleteRouteTable(
            @RequestParam String routeName,
            @RequestParam String userId) {
        try {
            // RouteTableService에서 직접 호출하여 라우트 테이블 삭제
            routeTableService.deleteRouteTable(routeName, userId);
            return responseService.getSuccessResult();
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("RouteTable deletion failed: " + e.getMessage());
        }
    }

    /**
     * RouteTable 조회 엔드포인트
     *
     * @param projectId GCP 프로젝트 ID
     * @return 조회된 라우트 테이블 목록
     */
    @GetMapping("/describe")
    public CommonResult fetchRouteTables(@RequestParam String projectId) {
        try {
            // GCP에서 라우트 테이블을 조회
            List<RoutePolicyDto> routePolicies = gcpResourceService.fetchRouteTables(projectId);
            return responseService.getListResult(routePolicies);  // 조회된 라우트 테이블 리스트 반환
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("RouteTable fetch failed: " + e.getMessage());
        }
    }
}
