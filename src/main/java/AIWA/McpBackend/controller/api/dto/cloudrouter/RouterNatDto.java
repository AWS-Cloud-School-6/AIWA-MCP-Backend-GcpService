package AIWA.McpBackend.controller.api.dto.cloudrouter;

import lombok.Getter;

import java.util.List;

@Getter
public class RouterNatDto {
    private String name;
    private String type;
    private String sourceSubnetworkIpRangesToNat;
    private boolean enableEndpointIndependentMapping;
    private String autoNetworkTier;
    private LogConfigDto logConfig;
    private String natIpAllocateOption;
    private List<String> endpointTypes;
    private boolean enableDynamicPortAllocation;

    // 생성자
    public RouterNatDto(String name, String type, String sourceSubnetworkIpRangesToNat,
                        boolean enableEndpointIndependentMapping, String autoNetworkTier,
                        LogConfigDto logConfig, String natIpAllocateOption,
                        List<String> endpointTypes, boolean enableDynamicPortAllocation) {
        this.name = name;
        this.type = type;
        this.sourceSubnetworkIpRangesToNat = sourceSubnetworkIpRangesToNat;
        this.enableEndpointIndependentMapping = enableEndpointIndependentMapping;
        this.autoNetworkTier = autoNetworkTier;
        this.logConfig = logConfig;
        this.natIpAllocateOption = natIpAllocateOption;
        this.endpointTypes = endpointTypes;
        this.enableDynamicPortAllocation = enableDynamicPortAllocation;
    }
}
