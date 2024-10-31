package AIWA.McpBackend.controller.api.restcontroller.ec2;

import AIWA.McpBackend.controller.api.dto.ec2.Ec2InstanceDTO;
import AIWA.McpBackend.controller.api.dto.ec2.Ec2RequestDto;
import AIWA.McpBackend.controller.api.dto.response.CommonResult;
import AIWA.McpBackend.controller.api.dto.response.ListResult;
import AIWA.McpBackend.service.aws.AwsResourceService;
import AIWA.McpBackend.service.aws.ec2.Ec2Service;
import AIWA.McpBackend.service.response.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/aws/api/ec2")
@RequiredArgsConstructor
public class Ec2Controller {

    private final Ec2Service ec2InstanceService;

    private final AwsResourceService awsResourceService;
    private final ResponseService responseService;

    @PostMapping("/create")
    public CommonResult createEC2Instance(@RequestBody Ec2RequestDto instanceRequest, @RequestParam String userId) {
        try {
            ec2InstanceService.createEC2Instance(instanceRequest, userId);
            return responseService.getSuccessResult();
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult();
        }
    }

    @DeleteMapping("/delete")
    public CommonResult deleteEC2Instance(@RequestParam String instanceName, @RequestParam String userId) {
        try {
            ec2InstanceService.deleteEC2Instance(instanceName, userId);
            return responseService.getSuccessResult();
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult();
        }
    }

    @GetMapping("/describe")
    public ListResult<Ec2InstanceDTO> describeEc2(@RequestParam String userId) {
        List<Ec2InstanceDTO> ec2Instances = awsResourceService.fetchEc2Instances(userId);
        return responseService.getListResult(ec2Instances);
    }
}