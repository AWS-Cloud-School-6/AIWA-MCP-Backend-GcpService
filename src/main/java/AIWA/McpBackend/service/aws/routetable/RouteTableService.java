package AIWA.McpBackend.service.aws.routetable;

import AIWA.McpBackend.service.aws.s3.S3Service;
import AIWA.McpBackend.service.terraform.TerraformService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RouteTableService {

    private final S3Service s3Service;
    private final TerraformService terraformService;

    /**
     * 라우트 테이블 생성
     */
    public void createRouteTable(String routeTableName, String vpcName, String userId) throws Exception {
        String routeTableTfContent = String.format("""
            resource "aws_route_table" "%s" {
              vpc_id = aws_vpc.%s.id
              tags = {
                Name = "%s"
              }
            }
        """, routeTableName, vpcName, routeTableName);

        String routeTableTfFileName = String.format("route-table_%s.tf", routeTableName);

        String s3Key = "users/" + userId + "/" + routeTableTfFileName;
        s3Service.uploadFileContent(s3Key, routeTableTfContent);

        terraformService.executeTerraform(userId);
    }

    /**
     * 라우트 테이블에 라우트 추가
     * 이 메서드는 여러 가지 유형의 게이트웨이 및 엔드포인트를 처리할 수 있도록 확장됩니다.
     */
    public void addRoute(String routeTableName, String destinationCidr, String gatewayType, String gatewayId, String userId) throws Exception {
        String routeResourceContent;

        // 게이트웨이 유형에 따라 Terraform 코드를 생성
        switch (gatewayType.toLowerCase()) {
            case "internet_gateway":
                routeResourceContent = String.format("""
                    resource "aws_route" "%s_route" {
                      route_table_id         = aws_route_table.%s.id
                      destination_cidr_block = "%s"
                      gateway_id             = aws_internet_gateway.%s.id
                    }
                """, routeTableName, routeTableName, destinationCidr, gatewayId);
                break;

            case "nat_gateway":
                routeResourceContent = String.format("""
                    resource "aws_route" "%s_route" {
                      route_table_id         = aws_route_table.%s.id
                      destination_cidr_block = "%s"
                      nat_gateway_id         = aws_nat_gateway.%s.id
                    }
                """, routeTableName, routeTableName, destinationCidr, gatewayId);
                break;

            case "vpc_peering":
                routeResourceContent = String.format("""
                    resource "aws_route" "%s_route" {
                      route_table_id         = aws_route_table.%s.id
                      destination_cidr_block = "%s"
                      vpc_peering_connection_id = aws_vpc_peering_connection.%s.id
                    }
                """, routeTableName, routeTableName, destinationCidr, gatewayId);
                break;

            case "interface_endpoint":
                routeResourceContent = String.format("""
                    resource "aws_route" "%s_route" {
                      route_table_id         = aws_route_table.%s.id
                      destination_cidr_block = "%s"
                      network_interface_id   = aws_network_interface.%s.id
                    }
                """, routeTableName, routeTableName, destinationCidr, gatewayId);
                break;

            default:
                throw new IllegalArgumentException("지원되지 않는 게이트웨이 유형입니다.");
        }

        // Terraform 파일 이름 설정
        String routeTfFileName = String.format("route_%s_%s.tf", routeTableName, gatewayType);

        // S3에 업로드
        String s3Key = "users/" + userId + "/" + routeTfFileName;
        s3Service.uploadFileContent(s3Key, routeResourceContent);

        // Terraform 실행
        terraformService.executeTerraform(userId);
    }

    /**
     * 라우트 테이블을 서브넷과 연결
     */
    public void associateRouteTableWithSubnet(String routeTableName, String subnetName, String userId) throws Exception {
        String associateTfContent = String.format("""
            resource "aws_route_table_association" "%s_assoc" {
              subnet_id      = aws_subnet.%s.id
              route_table_id = aws_route_table.%s.id
            }
        """, routeTableName, subnetName, routeTableName);

        String associateTfFileName = String.format("route_table_assoc_%s.tf", routeTableName);

        String s3Key = "users/" + userId + "/" + associateTfFileName;
        s3Service.uploadFileContent(s3Key, associateTfContent);

        terraformService.executeTerraform(userId);
    }
}