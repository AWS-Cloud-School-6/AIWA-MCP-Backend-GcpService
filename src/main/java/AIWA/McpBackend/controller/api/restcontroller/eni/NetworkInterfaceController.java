package AIWA.McpBackend.controller.api.restcontroller.eni;

import AIWA.McpBackend.controller.api.dto.eni.NetworkInterfaceDto;
import AIWA.McpBackend.controller.api.dto.response.ListResult;
import AIWA.McpBackend.service.aws.AwsResourceService;
import AIWA.McpBackend.service.response.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/aws/api/network-interface")
@RequiredArgsConstructor
public class NetworkInterfaceController {

    private final AwsResourceService awsResourceService;
    private final ResponseService responseService;

    @GetMapping("/describe")
    public ListResult<NetworkInterfaceDto> listNetworkInterfaces(@RequestParam String userId) {
        List<NetworkInterfaceDto> networkInterfaces = awsResourceService.fetchNetworkInterfaces(userId);
        return responseService.getListResult(networkInterfaces);
    }
}