package AIWA.McpBackend.controller.api.restcontroller.eip;

import AIWA.McpBackend.controller.api.dto.response.ListResult;
import AIWA.McpBackend.controller.api.dto.staticip.StaticIpDto;
import AIWA.McpBackend.service.gcp.GcpResourceService;
//import AIWA.McpBackend.service.gcp.eip.EipService;
import AIWA.McpBackend.service.response.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gcp/api/eip")
@RequiredArgsConstructor
public class EipController {

//    private final EipService eipService;
    @Autowired
    private final GcpResourceService gcpResourceService;
    private final ResponseService responseService;

//    /**
//     * EIP 생성 엔드포인트
//     *
//     * @param eipRequestDto 요청 DTO (사용자 ID, EC2 인스턴스 ID 포함)
//     * @return 성공 또는 오류 메시지
//     */
//    @PostMapping("/create")
//    public CommonResult createEip(@RequestBody EipRequestDto eipRequestDto) {
//        try {
//            eipService.createEip(eipRequestDto.getEipId(),eipRequestDto.getUserId());
//            return responseService.getSuccessResult();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return responseService.getFailResult();
//        }
//    }
//
//    /**
//     * EIP 삭제 엔드포인트
//     *
//     * @param eipRequestDto 요청 DTO (사용자 ID, EIP ID 포함)
//     * @return 성공 또는 오류 메시지
//     */
//    @DeleteMapping("/delete")
//    public CommonResult deleteEip(@RequestBody EipRequestDto eipRequestDto) {
//        try {
//            eipService.deleteEip(eipRequestDto.getUserId(), eipRequestDto.getEipId());
//            return responseService.getSuccessResult();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return responseService.getFailResult();
//        }
//    }

    @GetMapping("/describe")
    public ListResult<StaticIpDto> getStaticIps(
            @RequestParam String projectId) {  // projectId 파라미터 받기

        // gcpResourceService.getStaticIpsFromGCP에서 반환된 List<StaticIpInfoDTO>를 가져옵니다.
        List<StaticIpDto> staticIps = gcpResourceService.getStaticIpsFromGCP(projectId);

        // List<StaticIpInfoDTO>를 ResponseService의 getListResult를 사용하여 감싸서 반환합니다.
        return responseService.getListResult(staticIps);
    }
}