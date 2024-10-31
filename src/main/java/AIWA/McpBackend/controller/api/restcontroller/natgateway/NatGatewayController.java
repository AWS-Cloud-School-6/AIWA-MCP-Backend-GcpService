package AIWA.McpBackend.controller.api.restcontroller.natgateway;

import AIWA.McpBackend.controller.api.dto.natgateway.NatGatewayDto;
import AIWA.McpBackend.controller.api.dto.natgateway.NatGatewayRequestDto;
import AIWA.McpBackend.controller.api.dto.response.CommonResult;
import AIWA.McpBackend.controller.api.dto.response.ListResult;
import AIWA.McpBackend.service.aws.AwsResourceService;
import AIWA.McpBackend.service.aws.natgateway.NatGatewayService;
import AIWA.McpBackend.service.response.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/aws/api/nat-gateway")
@RequiredArgsConstructor
public class NatGatewayController {

    private final NatGatewayService natGatewayService;

    private final AwsResourceService awsResourceService;
    private final ResponseService responseService;

    @PostMapping("/create")
    public CommonResult createNatGateway(@RequestBody NatGatewayRequestDto natGatewayRequest, @RequestParam String userId) {
        try {
            natGatewayService.createNatGateway(natGatewayRequest, userId);
            return responseService.getSuccessResult();
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult();
        }
    }

    @DeleteMapping("/delete")
    public CommonResult deleteNatGateway(@RequestParam String natGatewayName, @RequestParam String userId) {
        try {
            natGatewayService.deleteNatGateway(natGatewayName, userId);
            return responseService.getSuccessResult();
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult();
        }
    }

    @PostMapping("/add-route")
    public CommonResult addRouteToNatGateway(@RequestParam String routeTableName, @RequestParam String natGatewayName, @RequestParam String cidrBlock, @RequestParam String userId) {
        try {
            natGatewayService.addRouteToNatGateway(routeTableName, natGatewayName, cidrBlock, userId);
            return responseService.getSuccessResult();
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult();
        }
    }

    @GetMapping("/describe")
    public ListResult<NatGatewayDto> describeNatGateway(@RequestParam String userId) {
        List<NatGatewayDto> natGateways = awsResourceService.fetchNatGateways(userId);
        return responseService.getListResult(natGateways);
    }
}