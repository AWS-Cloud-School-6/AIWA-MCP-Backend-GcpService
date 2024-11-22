package AIWA.McpBackend.controller.api.restcontroller.cloudnat;

import AIWA.McpBackend.controller.api.dto.cloudnat.CloudNatDto;
import AIWA.McpBackend.controller.api.dto.response.ListResult;
import AIWA.McpBackend.service.gcp.GcpResourceService;
//import AIWA.McpBackend.service.gcp.natgateway.NatGatewayService;
import AIWA.McpBackend.service.response.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gcp/api/cloud-nat")
@RequiredArgsConstructor
public class CloudNatController {

//    private final NatGatewayService natGatewayService;

    private final GcpResourceService gcpResourceService;
    private final ResponseService responseService;

//    @PostMapping("/create")
//    public CommonResult createNatGateway(@RequestBody NatGatewayRequestDto natGatewayRequest, @RequestParam String userId) {
//        try {
//            natGatewayService.createNatGateway(natGatewayRequest, userId);
//            return responseService.getSuccessResult();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return responseService.getFailResult();
//        }
//    }
//
//    @DeleteMapping("/delete")
//    public CommonResult deleteNatGateway(@RequestParam String natGatewayName, @RequestParam String userId) {
//        try {
//            natGatewayService.deleteNatGateway(natGatewayName, userId);
//            return responseService.getSuccessResult();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return responseService.getFailResult();
//        }
//    }
//
//    @PostMapping("/add-route")
//    public CommonResult addRouteToNatGateway(@RequestParam String routeTableName, @RequestParam String natGatewayName, @RequestParam String cidrBlock, @RequestParam String userId) {
//        try {
//            natGatewayService.addRouteToNatGateway(routeTableName, natGatewayName, cidrBlock, userId);
//            return responseService.getSuccessResult();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return responseService.getFailResult();
//        }
//    }

    @GetMapping("/describe")
    public ListResult<CloudNatDto> describeNatGateway(@RequestParam String projectId, @RequestParam String region) {
        List<CloudNatDto> cloudNatDetails = gcpResourceService.fetchCloudNatDetails(projectId, region);
        return responseService.getListResult(cloudNatDetails);
    }
}