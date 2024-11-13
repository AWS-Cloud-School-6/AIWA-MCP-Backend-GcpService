package AIWA.McpBackend.controller.api.restcontroller.vm;

import AIWA.McpBackend.service.gcp.GcpResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gcp/api/vm")
public class VmController {

    @Autowired
    private GcpResourceService gcpResourceService;

    @GetMapping("/describe")
    public ResponseEntity<?> listInstances(@RequestParam String projectId) {
        return gcpResourceService.listInstances(projectId);
    }
}