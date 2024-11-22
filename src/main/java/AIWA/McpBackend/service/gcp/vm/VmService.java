package AIWA.McpBackend.service.gcp.vm;

import AIWA.McpBackend.controller.api.dto.vm.VmRequestDto;
import AIWA.McpBackend.service.gcp.s3.S3Service;
import AIWA.McpBackend.service.terraform.TerraformService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VmService {

    private final S3Service s3Service;
    private final TerraformService terraformService;

    /**
     * GCP VM 생성
     *
     * @param vmRequest VM 생성 요청 DTO
     * @param userId    사용자 ID
     * @throws Exception VM 생성 중 발생한 예외
     */
    public void createVm(VmRequestDto vmRequest, String userId, String projectId) throws Exception {
        // 1. VM .tf 파일 생성
        String vmTfContent = String.format("""
                        resource "google_compute_instance" "%s" {
                          name         = "%s"
                          machine_type = "%s"
                          zone         = "%s"
                          
                          network_interface {
                            network    = "projects/%s/global/networks/%s"
                            subnetwork = "projects/%s/regions/asia-northeast3/subnetworks/%s"
                          }
                          
                          boot_disk {
                            initialize_params {
                              image = "projects/%s/global/images/family/%s"
                              size  = "%s"
                              type  = "%s"
                            }
                          }
                        }
                """,
                vmRequest.getVmName(),
                vmRequest.getVmName(),
                vmRequest.getMachineType(),
                vmRequest.getZone(),
                projectId,                 // 사용자 ID를 통해 프로젝트 ID 사용
                vmRequest.getNetworkName(),
                projectId,                 // 사용자 ID를 통해 프로젝트 ID 사용
                vmRequest.getSubnetworkName(),
                vmRequest.getImageProject(),
                vmRequest.getImageFamily(),
                vmRequest.getDiskSizeGb(),
                vmRequest.getDiskType()
        );

        // 2. VM .tf 파일 이름 설정 (예: vm_myVm.tf)
        String vmTfFileName = String.format("vm_%s.tf", vmRequest.getVmName());

        // 3. GCS에 새로운 VM .tf 파일 업로드
        String gcsKey = "users/" + userId + "/GCP/" + vmTfFileName;
        s3Service.uploadFileContent(gcsKey, vmTfContent);

        // 4. Terraform 실행 요청
        terraformService.executeTerraform(userId);
    }

    /**
     * GCP VM 삭제
     *
     * @param vmName VM 이름
     * @param userId 사용자 ID
     * @throws Exception VM 삭제 중 발생한 예외
     */
    public void deleteVm(String vmName, String userId) throws Exception {
        // 1. 삭제하려는 VM .tf 파일 이름 설정 (예: vm_myVm.tf)
        String vmTfFileName = String.format("vm_%s.tf", vmName);

        // 2. GCS에서 해당 VM .tf 파일 삭제
        String gcsKey = "users/" + userId + "/GCP/" + vmTfFileName;
        s3Service.deleteFile(gcsKey);

        // 3. Terraform 실행 요청
        terraformService.executeTerraform(userId);
    }
}
