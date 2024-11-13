package AIWA.McpBackend.controller.api.restcontroller.firewallpolicy;

import AIWA.McpBackend.controller.api.dto.securitygroup.FireWallPolicyDto;
import AIWA.McpBackend.controller.api.dto.securitygroup.SecurityGroupDTO;
import AIWA.McpBackend.controller.api.dto.response.ListResult;
import AIWA.McpBackend.service.gcp.GcpResourceService;
//import AIWA.McpBackend.service.gcp.securitygroup.SecurityGroupService;
import AIWA.McpBackend.service.response.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gcp/api/firewall-policy")
@RequiredArgsConstructor
public class FireWallPolicyController {

//    private final SecurityGroupService securityGroupService;

    private final GcpResourceService gcpResourceService;
    private final ResponseService responseService;

//    @PostMapping("/create")
//    public CommonResult createSecurityGroup(@RequestBody SecurityGroupRequestDto securityGroupRequest, @RequestParam String userId) {
//        try {
//            securityGroupService.createSecurityGroup(securityGroupRequest, userId);
//            return responseService.getSuccessResult();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return responseService.getFailResult();
//        }
//    }
//
//    @DeleteMapping("/delete")
//    public CommonResult deleteSecurityGroup(@RequestParam String securityGroupName, @RequestParam String userId) {
//        try {
//            securityGroupService.deleteSecurityGroup(securityGroupName, userId);
//            return responseService.getSuccessResult();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return responseService.getFailResult();
//        }
//    }

    @GetMapping("/describe")
    public ListResult<FireWallPolicyDto> getFirewallPolicies(@RequestParam String projectId) {
        return gcpResourceService.getFirewallRules(projectId);
    }
}
