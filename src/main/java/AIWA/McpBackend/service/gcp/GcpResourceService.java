package AIWA.McpBackend.service.gcp;


import AIWA.McpBackend.controller.api.dto.ec2.Ec2InstanceDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GcpResourceService {

    private final RestTemplate restTemplate;

    private final String projectId = "your-project-id"; // GCP 프로젝트 ID
    private final String zone = "us-central1-a"; // GCP 리소스가 존재하는 기본 존

    // 인스턴스 정보 조회 (AWS의 EC2에 해당)
    public List<Ec2InstanceDTO> fetchInstances() throws IOException {
        List<Ec2InstanceDTO> instances = new ArrayList<>();

        try (InstancesClient instancesClient = InstancesClient.create()) {
            for (Instance instance : instancesClient.list(projectId, zone).iterateAll()) {
                String instanceId = instance.getId();
                String status = instance.getStatus();
                Map<String, String> tagsMap = instance.getLabelsMap();

                instances.add(new Ec2InstanceDTO(instanceId, status, tagsMap));
            }
        }

        return instances;
    }


}
