package AIWA.McpBackend.controller.api.restcontroller.securitygroup;

import AIWA.McpBackend.controller.api.dto.securitygroup.SecurityGroupDTO;
import AIWA.McpBackend.controller.api.dto.securitygroup.SecurityGroupRequestDto;
import AIWA.McpBackend.controller.api.dto.response.CommonResult;
import AIWA.McpBackend.controller.api.dto.response.ListResult;
import AIWA.McpBackend.service.aws.AwsResourceService;
import AIWA.McpBackend.service.aws.securitygroup.SecurityGroupService;
import AIWA.McpBackend.service.response.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/aws/api/security-group")
@RequiredArgsConstructor
public class SecurityGroupController {

    private final SecurityGroupService securityGroupService;

    private final AwsResourceService awsResourceService;
    private final ResponseService responseService;

    @PostMapping("/create")
    public CommonResult createSecurityGroup(@RequestBody SecurityGroupRequestDto securityGroupRequest, @RequestParam String userId) {
        try {
            securityGroupService.createSecurityGroup(securityGroupRequest, userId);
            return responseService.getSuccessResult();
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult();
        }
    }

    @DeleteMapping("/delete")
    public CommonResult deleteSecurityGroup(@RequestParam String securityGroupName, @RequestParam String userId) {
        try {
            securityGroupService.deleteSecurityGroup(securityGroupName, userId);
            return responseService.getSuccessResult();
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult();
        }
    }

    @GetMapping("/describe")
    public ListResult<SecurityGroupDTO> describeSecurityGroup(@RequestParam String userId) {
        List<SecurityGroupDTO> securityGroups = awsResourceService.fetchSecurityGroups(userId);
        return responseService.getListResult(securityGroups);
    }
}
