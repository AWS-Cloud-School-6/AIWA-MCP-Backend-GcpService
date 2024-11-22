package AIWA.McpBackend.controller.api.restcontroller.cloudnat;

import AIWA.McpBackend.controller.api.dto.cloudnat.CloudNatDto;
import AIWA.McpBackend.controller.api.dto.cloudnat.CloudNatRequestDto;
import AIWA.McpBackend.controller.api.dto.response.CommonResult;
import AIWA.McpBackend.controller.api.dto.response.ListResult;
import AIWA.McpBackend.service.gcp.GcpResourceService;
//import AIWA.McpBackend.service.gcp.natgateway.NatGatewayService;
import AIWA.McpBackend.service.gcp.cloudnat.CloudNatService;
import AIWA.McpBackend.service.response.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gcp/api/cloud-nat")
@RequiredArgsConstructor
public class CloudNatController {

    private final CloudNatService cloudNatService;

    private final GcpResourceService gcpResourceService;
    private final ResponseService responseService;

    @PostMapping("/create")
    public CommonResult createNatGateway(@RequestBody CloudNatRequestDto cloudNatRequest, @RequestParam String userId) {
        try {
            cloudNatService.createCloudNat(cloudNatRequest, userId);
            return responseService.getSuccessResult();
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("CloudNat creation failed: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public CommonResult deleteCloudNat(@RequestParam String natGatewayName, @RequestParam String userId) {
        try {
            cloudNatService.deleteCloudNat(natGatewayName, userId);
            return responseService.getSuccessResult();
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("CloudNat creation failed: " + e.getMessage());
        }
    }

    @GetMapping("/describe")
    public ListResult<CloudNatDto> describeNatGateway(@RequestParam String projectId, @RequestParam String region) {
        List<CloudNatDto> cloudNatDetails = gcpResourceService.fetchCloudNatDetails(projectId, region);
        return responseService.getListResult(cloudNatDetails);
    }
}