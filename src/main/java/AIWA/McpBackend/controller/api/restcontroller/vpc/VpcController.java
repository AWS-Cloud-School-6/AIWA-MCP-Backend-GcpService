package AIWA.McpBackend.controller.api.restcontroller.vpc;

import AIWA.McpBackend.controller.api.dto.vpc.VpcTotalResponseDto;
import AIWA.McpBackend.controller.api.dto.vpc.VpcRequestDto;
import AIWA.McpBackend.controller.api.dto.response.CommonResult;
import AIWA.McpBackend.controller.api.dto.response.ListResult;
import AIWA.McpBackend.service.gcp.GcpResourceService;
import AIWA.McpBackend.service.gcp.vpc.VpcService;
import AIWA.McpBackend.service.response.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gcp/api/vpc") // GCP용 API 경로로 변경
@RequiredArgsConstructor
public class VpcController {

    private final VpcService vpcService;
    private final GcpResourceService gcpResourceService;
    private final ResponseService responseService;

    /**
     * VPC 생성 엔드포인트
     *
     * @param vpcRequest VPC 생성 요청 DTO
     * @param userId     사용자 ID (요청 파라미터로 전달)
     * @return 생성 성공 메시지 또는 오류 메시지
     */
    @PostMapping("/create")
    public CommonResult createVpc(
            @RequestBody VpcRequestDto vpcRequest,
            @RequestParam String userId) {
        try {
            vpcService.createVpc(vpcRequest, userId);
            return responseService.getSuccessResult();
        } catch (Exception e) {
            // 예외 로그 기록 (추가적인 로깅 프레임워크 사용 권장)
            e.printStackTrace();
            return responseService.getFailResult();
        }
    }

    /**
     * VPC 삭제 엔드포인트
     *
     * @param vpcName VPC 이름 (요청 파라미터로 전달)
     * @param userId  사용자 ID (요청 파라미터로 전달)
     * @return 삭제 성공 메시지 또는 오류 메시지
     */
    @DeleteMapping("/delete")
    public CommonResult deleteVpc(
            @RequestParam String vpcName,
            @RequestParam String userId) {
        try {
            vpcService.deleteVpc(vpcName, userId);
            return responseService.getSuccessResult();
        } catch (Exception e) {
            // 예외 로그 기록 (추가적인 로깅 프레임워크 사용 권장)
            e.printStackTrace();
            return responseService.getFailResult();
        }
    }

    /**
     * VPC 정보 조회 엔드포인트
     *
     * @param userId 사용자 ID (요청 파라미터로 전달)
     * @return VPC 목록 및 정보
     */
    @GetMapping("/describe")
    public ListResult<VpcTotalResponseDto> describeVpc(@RequestParam String userId) {
        try {
            // VPCs - 서브넷 및 라우팅 테이블 정보 전달
            List<VpcTotalResponseDto> vpcs = gcpResourceService.fetchVpcs(userId);
            return responseService.getListResult(vpcs);
        } catch (Exception e) {
            // 예외 로그 기록
            e.printStackTrace();
            return responseService.getFailResult();
        }
    }
}