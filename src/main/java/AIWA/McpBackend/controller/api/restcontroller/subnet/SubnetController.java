package AIWA.McpBackend.controller.api.restcontroller.subnet;

import AIWA.McpBackend.controller.api.dto.subnet.SubnetResponseDto;
import AIWA.McpBackend.controller.api.dto.subnet.SubnetRequestDto;
import AIWA.McpBackend.controller.api.dto.response.CommonResult;
import AIWA.McpBackend.controller.api.dto.response.ListResult;
import AIWA.McpBackend.service.aws.AwsResourceService;
import AIWA.McpBackend.service.aws.subnet.SubnetService;
import AIWA.McpBackend.service.response.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/aws/api/subnet")
@RequiredArgsConstructor
public class SubnetController {

    private final SubnetService subnetService;
    private final AwsResourceService awsResourceService;
    private final ResponseService responseService;

    @PostMapping("/create")
    public CommonResult createSubnet(@RequestBody SubnetRequestDto subnetRequest, @RequestParam String userId) {
        try {
            subnetService.createSubnet(subnetRequest, userId);
            return responseService.getSuccessResult();
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult();
        }
    }

    @DeleteMapping("/delete")
    public CommonResult deleteSubnet(@RequestParam String subnetName, @RequestParam String userId) {
        try {
            subnetService.deleteSubnet(subnetName, userId);
            return responseService.getSuccessResult();
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult();
        }
    }

    @GetMapping("/describe")
    public ListResult<SubnetResponseDto> describeSubnet(@RequestParam String userId) {

        List<SubnetResponseDto> subnets = awsResourceService.fetchSubnets(userId);
        return responseService.getListResult(subnets);
    }
}