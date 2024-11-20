package AIWA.McpBackend.controller.api.restcontroller.cloudrouter;

import AIWA.McpBackend.controller.api.dto.cloudrouter.CloudRouterDto;
import AIWA.McpBackend.controller.api.dto.response.ListResult;
import AIWA.McpBackend.service.gcp.GcpResourceService;
//import AIWA.McpBackend.service.gcp.internetgateway.InternetGatewayService;
import AIWA.McpBackend.service.response.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gcp/api/cloud-router")
@RequiredArgsConstructor
public class CloudRouterController {

//    private final InternetGatewayService internetGatewayService;

    private final ResponseService responseService;

    private final GcpResourceService gcpResourceService;
//    @PostMapping("/create")
//    public CommonResult createInternetGateway(@RequestBody InternetGatewayRequestDto igwRequest, @RequestParam String userId) {
//        try {
//            internetGatewayService.createInternetGateway(igwRequest, userId);
//            return responseService.getSuccessResult();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return responseService.getFailResult();
//        }
//    }
//
//    @DeleteMapping("/delete")
//    public CommonResult deleteInternetGateway(@RequestParam String igwName, @RequestParam String userId) {
//        try {
//            internetGatewayService.deleteInternetGateway(igwName, userId);
//            return responseService.getSuccessResult();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return responseService.getFailResult();
//        }
//    }

    @GetMapping("/describe")
    public ListResult<CloudRouterDto> describeCloudRouter(@RequestParam String projectId, @RequestParam String region) {
        // GCP 리소스 서비스에서 CloudRouter 정보를 가져옴
        List<CloudRouterDto> cloudRouters = gcpResourceService.fetchCloudRouterInfo(projectId, region);

        // 응답 형식에 맞게 결과 반환
        return responseService.getListResult(cloudRouters);
    }
}