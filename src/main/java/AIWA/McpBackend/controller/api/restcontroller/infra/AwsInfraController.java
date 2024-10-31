package AIWA.McpBackend.controller.api.restcontroller.infra;

import AIWA.McpBackend.controller.api.dto.infra.InfraDeleteRequestDto;
import AIWA.McpBackend.controller.api.dto.infra.InfraRequestDto;
import AIWA.McpBackend.controller.api.dto.subnet.SubnetRequestDto;
import AIWA.McpBackend.controller.api.dto.response.CommonResult;
import AIWA.McpBackend.service.aws.subnet.SubnetService;
import AIWA.McpBackend.service.aws.vpc.VpcService;
import AIWA.McpBackend.service.response.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/aws/api/aws")
@RequiredArgsConstructor
public class AwsInfraController {

    private final VpcService vpcService;
    private final SubnetService subnetService;

    private final ResponseService responseService;

    /**
     * VPC 및 Subnet을 생성합니다.
     *
     * @param infraRequest VPC 및 Subnet 생성 요청 DTO
     * @param userId 사용자 ID
     * @return 생성 결과 메시지
     */
    @PostMapping("/infra")
    public CommonResult createVpcAndSubnets(@RequestBody InfraRequestDto infraRequest,
                                            @RequestParam String userId) {
        try {
            // 1. VPC 생성
            vpcService.createVpc(infraRequest.getVpcRequest(), userId);
            // 2. Subnet 생성
            for (SubnetRequestDto subnetRequest : infraRequest.getSubnetRequests()) {
                subnetService.createSubnet(subnetRequest, userId);
            }
            return responseService.getSuccessResult();
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult();
        }
    }

    /**
     * VPC 및 Subnet을 삭제합니다.
     *
     * @param infraDeleteRequest 삭제 요청 DTO
     * @param userId 사용자 ID
     * @return 삭제 결과 메시지
     */
    @DeleteMapping("/infra")
    public CommonResult deleteVpcAndSubnets(@RequestBody InfraDeleteRequestDto infraDeleteRequest,
                                                      @RequestParam String userId) {
        try {
            // 1. Subnet 삭제
            for (String subnetName : infraDeleteRequest.getSubnetNames()) {
                subnetService.deleteSubnet(subnetName, userId);
            }
            // 2. VPC 삭제
            vpcService.deleteVpc(infraDeleteRequest.getVpcName(), userId);
            return responseService.getSuccessResult();

        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult();
        }
    }
}