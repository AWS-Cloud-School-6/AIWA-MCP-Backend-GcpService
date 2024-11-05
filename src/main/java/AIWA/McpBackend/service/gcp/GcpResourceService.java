package AIWA.McpBackend.service.gcp;

import AIWA.McpBackend.controller.api.dto.vpc.VpcTotalResponseDto;
import com.google.cloud.compute.v1.ListNetworksRequest;
import com.google.cloud.compute.v1.Network;
import com.google.cloud.compute.v1.NetworksClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class GcpResourceService {

    /**
     * 특정 GCP 프로젝트의 VPC 네트워크 목록을 조회하여 VpcTotalResponseDto로 변환
     * @param projectId GCP 프로젝트 ID
     * @return List of VpcTotalResponseDto
     * @throws IOException
     */
    public List<VpcTotalResponseDto> fetchVpcs(String projectId) throws IOException {
        List<VpcTotalResponseDto> vpcList = new ArrayList<>();

        // NetworksClient를 사용하여 GCP 네트워크 조회
        try (NetworksClient networksClient = NetworksClient.create()) {
            ListNetworksRequest request = ListNetworksRequest.newBuilder().setProject(projectId).build();

            for (Network network : networksClient.list(request).iterateAll()) {
                // Network 객체를 VpcTotalResponseDto로 변환
                VpcTotalResponseDto vpcDto = new VpcTotalResponseDto(
                        network.getSelfLink(), // networkId 대신 selfLink를 사용
                        network.getName(), // name
                        network.getSelfLink(), // selfLink
                        network.getSubnetworksList() // subnetworks
                );
                vpcList.add(vpcDto);
            }
        }
        return vpcList;
    }
}