package AIWA.McpBackend.service.gcp.routetable;

import AIWA.McpBackend.controller.api.dto.routetable.RouteTableRequestDto;
import AIWA.McpBackend.service.gcp.s3.S3Service;
import AIWA.McpBackend.service.terraform.TerraformService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RouteTableService {

    private final S3Service s3Service;
    private final TerraformService terraformService;

    /**
     * GCP RouteTable 생성
     *
     * @param routeRequest RouteTable 생성 요청 DTO
     * @param userId      사용자 ID
     * @throws Exception RouteTable 생성 중 발생한 예외
     */
    public void createRouteTable(RouteTableRequestDto routeRequest, String userId) throws Exception {
        // 1. RouteTable .tf 파일 생성
        String routeTfContent = String.format("""
                resource "google_compute_route" "%s" {
                  name                  = "%s"
                  destination_range     = "%s"
                  network               = "projects/%s/global/networks/%s"
                  next_hop_gateway      = "%s"
                  priority              = %d
                }
                """,
                routeRequest.getName(),
                routeRequest.getName(),
                routeRequest.getDestinationRange(),
                userId,  // 사용자 ID를 통해 프로젝트 ID 사용
                routeRequest.getNetworkName(),
                routeRequest.getNextHop(),
                routeRequest.getPriority()
        );

        // 2. RouteTable .tf 파일 이름 설정 (예: route_myRoute.tf)
        String routeTfFileName = String.format("route_%s.tf", routeRequest.getName());

        // 3. GCS에 새로운 RouteTable .tf 파일 업로드
        String gcsKey = "users/" + userId + "/GCP/" + routeTfFileName;
        s3Service.uploadFileContent(gcsKey, routeTfContent);

        // 4. Terraform 실행 요청
        terraformService.executeTerraform(userId);
    }

    /**
     * GCP RouteTable 삭제
     *
     * @param routeName RouteTable 이름
     * @param userId    사용자 ID
     * @throws Exception RouteTable 삭제 중 발생한 예외
     */
    public void deleteRouteTable(String routeName, String userId) throws Exception {
        // 1. 삭제하려는 RouteTable .tf 파일 이름 설정 (예: route_myRoute.tf)
        String routeTfFileName = String.format("route_%s.tf", routeName);

        // 2. GCS에서 해당 RouteTable .tf 파일 삭제
        String gcsKey = "users/" + userId + "/GCP/" + routeTfFileName;
        s3Service.deleteFile(gcsKey);

        // 3. Terraform 실행 요청
        terraformService.executeTerraform(userId);
    }
}
