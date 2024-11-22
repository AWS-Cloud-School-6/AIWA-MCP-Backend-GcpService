package AIWA.McpBackend.controller.api.restcontroller.cloudrouter;

import AIWA.McpBackend.controller.api.dto.cloudrouter.CloudRouterDto;
import AIWA.McpBackend.controller.api.dto.cloudrouter.CloudRouterRequestDto;
import AIWA.McpBackend.controller.api.dto.response.CommonResult;
import AIWA.McpBackend.controller.api.dto.response.ListResult;
import AIWA.McpBackend.service.gcp.GcpResourceService;
import AIWA.McpBackend.service.gcp.cloudrouter.CloudRouterService;
import AIWA.McpBackend.service.response.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gcp/api/cloud-router")
@RequiredArgsConstructor
public class CloudRouterController {

    private final CloudRouterService cloudRouterService;

    private final ResponseService responseService;

    private final GcpResourceService gcpResourceService;
    @PostMapping("/create")
    public CommonResult createCloudRouter(@RequestBody CloudRouterRequestDto cloudRouterRequest, @RequestParam String userId) {
        try {
            cloudRouterService.createCloudRouter(cloudRouterRequest, userId);
            return responseService.getSuccessResult();
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("CloudRouter creation failed: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public CommonResult deleteCloudRouter(@RequestParam String igwName, @RequestParam String userId) {
        try {
            cloudRouterService.deleteCloudRouter(igwName, userId);
            return responseService.getSuccessResult();
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("CloudRouter creation failed: " + e.getMessage());
        }
    }

    @GetMapping("/describe")
    public ListResult<CloudRouterDto> describeCloudRouter(@RequestParam String projectId,
                                                          @RequestParam String region,
                                                          @RequestParam String userId) {
        // GCP 리소스 서비스에서 CloudRouter 정보를 가져옴
        List<CloudRouterDto> cloudRouters = gcpResourceService.fetchCloudRouterInfo(projectId, region, userId);

        // 응답 형식에 맞게 결과 반환
        return responseService.getListResult(cloudRouters);
    }
}