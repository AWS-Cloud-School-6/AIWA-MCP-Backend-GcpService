package AIWA.McpBackend.controller.api.restcontroller.routetable;

import AIWA.McpBackend.controller.api.dto.routetable.RouteAddRequestDto;
import AIWA.McpBackend.controller.api.dto.routetable.RouteTableRequestDto;
import AIWA.McpBackend.controller.api.dto.routetable.RouteTableResponseDto;
import AIWA.McpBackend.controller.api.dto.routetable.RouteTableSubnetAssociationRequestDto;
import AIWA.McpBackend.controller.api.dto.response.CommonResult;
import AIWA.McpBackend.controller.api.dto.response.ListResult;
import AIWA.McpBackend.service.aws.AwsResourceService;
import AIWA.McpBackend.service.aws.routetable.RouteTableService;
import AIWA.McpBackend.service.response.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/aws/api/route-table")
@RequiredArgsConstructor
public class RouteTableController {

    private final RouteTableService routeTableService;

    private final AwsResourceService awsResourceService;
    private final ResponseService responseService;

    /**
     * 라우트 테이블 생성
     */
    @PostMapping("/create")
    public CommonResult createRouteTable(@RequestBody RouteTableRequestDto routeTableRequestDto) {
        try {
            routeTableService.createRouteTable(
                    routeTableRequestDto.getRouteTableName(),
                    routeTableRequestDto.getVpcName(),
                    routeTableRequestDto.getUserId()
            );
            return responseService.getSuccessResult();
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult();
        }
    }
    /**
     * 라우트 테이블에 게이트웨이 또는 엔드포인트로 라우트 추가
     */
    @PostMapping("/add-route")
    public CommonResult addRoute(@RequestBody RouteAddRequestDto routeAddRequestDto) {
        try {
            routeTableService.addRoute(
                    routeAddRequestDto.getRouteTableName(),
                    routeAddRequestDto.getDestinationCidr(),
                    routeAddRequestDto.getGatewayType(),
                    routeAddRequestDto.getGatewayId(),
                    routeAddRequestDto.getUserId()
            );
            return responseService.getSuccessResult();
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult();
        }
    }

    /**
     * 라우트 테이블을 서브넷과 연결
     */
    @PostMapping("/associate-subnet")
    public CommonResult associateRouteTableWithSubnet(@RequestBody RouteTableSubnetAssociationRequestDto requestDto) {
        try {
            routeTableService.associateRouteTableWithSubnet(
                    requestDto.getRouteTableName(),
                    requestDto.getSubnetName(),
                    requestDto.getUserId()
            );
            return responseService.getSuccessResult();
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult();
        }
    }

    @GetMapping("/describe")
    public ListResult<RouteTableResponseDto> describeRouteTable(@RequestParam String userId) {
        List<RouteTableResponseDto> routeTables = awsResourceService.fetchRouteTables(userId);
        return responseService.getListResult(routeTables);
    }
}