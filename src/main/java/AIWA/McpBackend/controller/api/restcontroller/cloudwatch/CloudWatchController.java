package AIWA.McpBackend.controller.api.restcontroller.cloudwatch;

import AIWA.McpBackend.controller.api.dto.cloudwatch.CloudWatchAlarmRequestDto;
import AIWA.McpBackend.controller.api.dto.response.CommonResult;
import AIWA.McpBackend.service.aws.cloudwatch.CloudWatchService;
import AIWA.McpBackend.service.response.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/aws/api/cloudwatch")
@RequiredArgsConstructor
public class CloudWatchController {

    private final CloudWatchService cloudWatchService;

    private final ResponseService responseService;

    @PostMapping("/create-alarm")
    public CommonResult createCloudWatchAlarm(@RequestBody CloudWatchAlarmRequestDto alarmRequest, @RequestParam String userId) {
        try {
            cloudWatchService.createCloudWatchAlarm(alarmRequest, userId);
            return responseService.getSuccessResult();
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult();
        }
    }

    @DeleteMapping("/delete-alarm")
    public CommonResult deleteCloudWatchAlarm(@RequestParam String alarmName, @RequestParam String userId) {
        try {
            cloudWatchService.deleteCloudWatchAlarm(alarmName, userId);
            return responseService.getSuccessResult();
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult();
        }
    }
}