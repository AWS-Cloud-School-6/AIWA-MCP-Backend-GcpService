package AIWA.McpBackend.controller.api.dto.infra;

import AIWA.McpBackend.controller.api.dto.vpc.VpcRequestDto;
import AIWA.McpBackend.controller.api.dto.subnet.SubnetRequestDto;
import lombok.Data;

import java.util.List;

@Data
public class InfraRequestDto {
    private VpcRequestDto vpcRequest;
    private List<SubnetRequestDto> subnetRequests;
}