package AIWA.McpBackend.controller.api.restcontroller.vpc;

import AIWA.McpBackend.controller.api.dto.vpc.VpcTotalResponseDto;
import AIWA.McpBackend.controller.api.dto.vpc.VpcRequestDto;
import AIWA.McpBackend.controller.api.dto.response.CommonResult;
import AIWA.McpBackend.controller.api.dto.response.ListResult;
import AIWA.McpBackend.service.gcp.GcpResourceService;
import AIWA.McpBackend.service.gcp.vpc.VpcService;
import AIWA.McpBackend.service.response.ResponseService;
import com.google.cloud.compute.v1.Network;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/gcp/api/vpc")
@RequiredArgsConstructor
public class VpcController {

    private final VpcService vpcService;
    private final GcpResourceService gcpResourceService;
    private final ResponseService responseService;

    /**
     * VPC 생성 엔드포인트
     *
     * @param vpcRequest VPC 생성 요청 DTO
     * @param projectId  사용자 GCP 프로젝트 ID
     * @return 생성 성공 메시지 또는 오류 메시지
     */
    @PostMapping("/create")
    public CommonResult createVpc(
            @RequestBody VpcRequestDto vpcRequest,
            @RequestParam String projectId) {
        try {
            vpcService.createVpc(vpcRequest, projectId);
            return responseService.getSuccessResult();
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult();
        }
    }

    /**
     * VPC 삭제 엔드포인트
     *
     * @param vpcName   VPC 이름 (요청 파라미터로 전달)
     * @param projectId 사용자 GCP 프로젝트 ID
     * @return 삭제 성공 메시지 또는 오류 메시지
     */
    @DeleteMapping("/delete")
    public CommonResult deleteVpc(
            @RequestParam String vpcName,
            @RequestParam String projectId) {
        try {
            vpcService.deleteVpc(vpcName, projectId);
            return responseService.getSuccessResult();
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult();
        }
    }

    /**
     * VPC 정보 조회 엔드포인트
     *
     * @param projectId 사용자 GCP 프로젝트 ID
     * @return VPC 목록 및 정보
     */
    @GetMapping("/describe")
    public ListResult<VpcTotalResponseDto> describeVpc(@RequestParam String projectId) throws IOException {
        // VPCs - 서브넷 및 라우팅 테이블 정보 전달
        List<VpcTotalResponseDto> vpcTotalResponseDtos = gcpResourceService.fetchVpcs(projectId);
        return responseService.getListResult(vpcTotalResponseDtos);
    }
}