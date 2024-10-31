package AIWA.McpBackend.controller.api.restcontroller.eip;

import AIWA.McpBackend.controller.api.dto.eip.EipDto;
import AIWA.McpBackend.controller.api.dto.eip.EipRequestDto;
import AIWA.McpBackend.controller.api.dto.response.CommonResult;
import AIWA.McpBackend.controller.api.dto.response.ListResult;
import AIWA.McpBackend.service.aws.AwsResourceService;
import AIWA.McpBackend.service.aws.eip.EipService;
import AIWA.McpBackend.service.response.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/aws/api/eip")
@RequiredArgsConstructor
public class EipController {

    private final EipService eipService;
    private final AwsResourceService awsResourceService;
    private final ResponseService responseService;

    /**
     * EIP 생성 엔드포인트
     *
     * @param eipRequestDto 요청 DTO (사용자 ID, EC2 인스턴스 ID 포함)
     * @return 성공 또는 오류 메시지
     */
    @PostMapping("/create")
    public CommonResult createEip(@RequestBody EipRequestDto eipRequestDto) {
        try {
            eipService.createEip(eipRequestDto.getEipId(),eipRequestDto.getUserId());
            return responseService.getSuccessResult();
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult();
        }
    }

    /**
     * EIP 삭제 엔드포인트
     *
     * @param eipRequestDto 요청 DTO (사용자 ID, EIP ID 포함)
     * @return 성공 또는 오류 메시지
     */
    @DeleteMapping("/delete")
    public CommonResult deleteEip(@RequestBody EipRequestDto eipRequestDto) {
        try {
            eipService.deleteEip(eipRequestDto.getUserId(), eipRequestDto.getEipId());
            return responseService.getSuccessResult();
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult();
        }
    }

    @GetMapping("/describe")
    public ListResult<EipDto> describeEip(@RequestParam String userId) {
        List<EipDto> eips = awsResourceService.fetchElasticIps(userId);
        return responseService.getListResult(eips);
    }
}