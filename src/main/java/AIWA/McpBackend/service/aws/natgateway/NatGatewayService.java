package AIWA.McpBackend.service.aws.natgateway;

import AIWA.McpBackend.controller.api.dto.natgateway.NatGatewayRequestDto;
import AIWA.McpBackend.service.terraform.TerraformService;
import AIWA.McpBackend.service.aws.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NatGatewayService {

    private final S3Service s3Service;
    private final TerraformService terraformService;

    /**
     * NAT Gateway를 생성합니다.
     *
     * @param natGatewayRequest NAT Gateway 생성 요청 DTO
     * @param userId            사용자 ID
     * @throws Exception NAT Gateway 생성 중 발생한 예외
     */
    public void createNatGateway(NatGatewayRequestDto natGatewayRequest, String userId) throws Exception {
        // 1. 새로운 NAT Gateway .tf 파일 생성
        String allocationId = natGatewayRequest.getAllocationId();  // 추가된 allocation ID
        String elasticIpName = natGatewayRequest.getElasticIpName();  // Elastic IP 이름

        // 1-1. allocationId가 없을 경우, ElasticIpName을 사용
        String finalAllocationId;
        if (allocationId != null && !allocationId.isEmpty()) {
            finalAllocationId = allocationId;  // 사용자가 제공한 allocationId 사용
        } else if (elasticIpName != null && !elasticIpName.isEmpty()) {
            // Elastic IP 이름이 있을 경우, 해당 이름으로 참조
            finalAllocationId = "aws_eip." + elasticIpName + ".id";
        } else {
            throw new IllegalArgumentException("Either allocationId or elasticIpName must be provided.");
        }
        String natGatewayTfContent = String.format("""
            resource "aws_nat_gateway" "%s" {
              allocation_id = "%s"
              subnet_id = aws_subnet.%s.id
              tags = {
                Name = "%s"
              }
            }
            """,
                natGatewayRequest.getNatGatewayName(),
                finalAllocationId,  // allocationId 적용
                natGatewayRequest.getSubnetName(),
                natGatewayRequest.getNatGatewayName());

        // 2. NAT Gateway .tf 파일 이름 설정 (예: nat_gateway_myNatGateway.tf)
        String natGatewayTfFileName = String.format("nat_gateway_%s.tf", natGatewayRequest.getNatGatewayName());

        // 3. S3에 새로운 NAT Gateway .tf 파일 업로드
        String s3Key = "users/" + userId + "/" + natGatewayTfFileName;
        s3Service.uploadFileContent(s3Key, natGatewayTfContent);

        // 4. Terraform 실행 요청
        terraformService.executeTerraform(userId);
    }

    /**
     * NAT Gateway를 삭제합니다.
     *
     * @param natGatewayName NAT Gateway 이름
     * @param userId         사용자 ID
     * @throws Exception NAT Gateway 삭제 중 발생한 예외
     */
    public void deleteNatGateway(String natGatewayName, String userId) throws Exception {
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