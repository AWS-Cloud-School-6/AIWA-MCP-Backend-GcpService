//package AIWA.McpBackend.controller.api.restcontroller;
//
//import AIWA.McpBackend.controller.api.dto.ec2.Ec2InstanceDTO;
//import AIWA.McpBackend.controller.api.dto.securitygroup.SecurityGroupDTO;
//import AIWA.McpBackend.controller.api.dto.vpc.VpcTotalResponseDto;
//import AIWA.McpBackend.service.gcp.GcpResourceService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.*;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/aws")
//public class AwsResourceController {
//
//    private final GcpResourceService gcpResourceService;
//
//    @GetMapping("/api/aws/resources")
//    public Map<String, Object> getAwsResources(@RequestParam String userId) {
//        Map<String, Object> resources = new HashMap<>();
//        gcpResourceService.initializeClient(userId);
//
//        // EC2 Instances
//        List<Ec2InstanceDTO> ec2Instances = gcpResourceService.fetchEc2Instances(userId);
//        resources.put("ec2Instances", ec2Instances);
//
//        // VPCs - 서브넷 및 라우팅 테이블 정보 전달
//        List<VpcTotalResponseDto> vpcs = gcpResourceService.fetchVpcs(userId);
//        resources.put("vpcs", vpcs);
//
//        // Security Groups
//        List<SecurityGroupDTO> securityGroups = gcpResourceService.fetchSecurityGroups(userId);
//        resources.put("securityGroups", securityGroups);
//
//        return resources; // 자동으로 JSON 형식으로 변환되어 응답
//    }
//}
