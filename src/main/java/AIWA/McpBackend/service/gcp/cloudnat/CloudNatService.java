package AIWA.McpBackend.service.gcp.cloudnat;

import AIWA.McpBackend.controller.api.dto.cloudnat.CloudNatRequestDto;
import AIWA.McpBackend.controller.api.dto.natgateway.NatGatewayRequestDto;
import AIWA.McpBackend.service.terraform.TerraformService;
import AIWA.McpBackend.service.gcp.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CloudNatService {

    private final S3Service s3Service;
    private final TerraformService terraformService;

    /**
     * NAT Gateway를 생성합니다.
     *
     * @param cloudNatRequest NAT Gateway 생성 요청 DTO
     * @param userId            사용자 ID
     * @throws Exception NAT Gateway 생성 중 발생한 예외
     */
    public void createCloudNat(CloudNatRequestDto cloudNatRequest, String userId) throws Exception {
        // 1. GCP Cloud NAT .tf 파일 생성
        String cloudNatTfContent = String.format("""
            resource "google_compute_router" "%s" {
              name    = "%s"
              region  = "%s"
              network = "%s"
            }

            resource "google_compute_router_nat" "%s" {
              name                               = "%s"
              router                             = google_compute_router.%s.name
              region                             = "%s"
              nat_ip_allocate_option             = "AUTO_ONLY"
              source_subnetwork_ip_ranges_to_nat = "ALL_SUBNETWORKS_ALL_IP_RANGES"

              log_config {
                enable   = true
                severity = "INFO"
              }
            }
            """, cloudNatRequest.getRouterName(), cloudNatRequest.getRouterName(),
                cloudNatRequest.getRegion(), cloudNatRequest.getNetwork(),
                cloudNatRequest.getNatName(), cloudNatRequest.getNatName(),
                cloudNatRequest.getRouterName(), cloudNatRequest.getRegion());

        // 2. Cloud NAT .tf 파일 이름 설정 (예: cloud_nat_myNat.tf)
        String cloudNatTfFileName = String.format("cloud_nat_%s.tf", cloudNatRequest.getNatName());

        // 3. 콘솔에 내용 출력 (디버깅 용도)
        System.out.println(cloudNatTfContent);

        // 4. GCS에 새로운 Cloud NAT .tf 파일 업로드
        String gcsKey = "users/" + userId + "/GCP/" + cloudNatTfFileName;
        s3Service.uploadFileContent(gcsKey, cloudNatTfContent);

        // 5. Terraform 실행 요청
        terraformService.executeTerraform(userId);
    }

    /**
     * NAT Gateway를 삭제합니다.
     *
     * @param natGatewayName NAT Gateway 이름
     * @param userId         사용자 ID
     * @throws Exception NAT Gateway 삭제 중 발생한 예외
     */
    public void deleteCloudNat(String natGatewayName, String userId) throws Exception {
        // 1. 삭제하려는 NAT Gateway .tf 파일 이름 설정 (예: nat_gateway_myNatGateway.tf)
        String natGatewayTfFileName = String.format("nat_gateway_%s.tf", natGatewayName);

        // 2. S3에서 해당 NAT Gateway .tf 파일 삭제
        String s3Key = "users/" + userId + "/" + natGatewayTfFileName;
        s3Service.deleteFile(s3Key);

        // 3. Terraform 실행 요청
        terraformService.executeTerraform(userId);
    }

    /**
     * NAT Gateway로 라우트를 추가합니다.
     *
     * @param routeTableName Route Table 이름
     * @param natGatewayName NAT Gateway 이름
     * @param cidrBlock      CIDR 블록 (예: "0.0.0.0/0"은 모든 트래픽)
     * @param userId         사용자 ID
     * @throws Exception NAT Gateway로의 라우트 추가 중 발생한 예외
     */
    public void addRouteToNatGateway(String routeTableName, String natGatewayName, String cidrBlock, String userId) throws Exception {
        // 1. Route를 추가하는 코드 블록 생성
        String routeTfContent = String.format("""
                resource "aws_route" "route_to_nat" {
                  route_table_id = aws_route_table.%s.id
                  destination_cidr_block = "%s"
                  nat_gateway_id = aws_nat_gateway.%s.id
                }
                """, routeTableName, cidrBlock, natGatewayName);

        // 2. Route .tf 파일 이름 설정 (예: route_to_nat_gateway.tf)
        String routeTfFileName = String.format("route_%s_to_nat_gateway.tf", routeTableName);

        // 3. S3에 새로운 Route .tf 파일 업로드
        String s3Key = "users/" + userId + "/" + routeTfFileName;
        s3Service.uploadFileContent(s3Key, routeTfContent);

        // 4. Terraform 실행 요청
        terraformService.executeTerraform(userId);
    }
}