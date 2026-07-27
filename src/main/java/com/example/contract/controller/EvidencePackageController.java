package com.example.contract.controller;

import com.example.contract.dto.response.ApiResponse;
import com.example.contract.dto.response.EvidencePackageResponse;
import com.example.contract.service.EvidencePackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 证据包控制器
 */
@RestController
@RequestMapping("/api/evidence-packages")
@RequiredArgsConstructor
public class EvidencePackageController {

    private final EvidencePackageService evidencePackageService;

    /**
     * 生成证据包
     */
    @PostMapping("/contract/{contractId}")
    public ResponseEntity<ApiResponse<EvidencePackageResponse>> generateEvidencePackage(@PathVariable Long contractId) {
        EvidencePackageResponse response = evidencePackageService.generateEvidencePackage(contractId);
        return ResponseEntity.ok(ApiResponse.success("生成成功", response));
    }

    /**
     * 查询证据包列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<EvidencePackageResponse>>> listEvidencePackages(
            @RequestParam(required = false) Long contractId) {
        List<EvidencePackageResponse> response;
        if (contractId != null) {
            response = evidencePackageService.getEvidencePackagesByContract(contractId);
        } else {
            response = evidencePackageService.getAllEvidencePackages();
        }
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 根据证据包编号查询证据包
     */
    @GetMapping("/by-no/{packageNo}")
    public ResponseEntity<ApiResponse<EvidencePackageResponse>> getEvidencePackageByNo(@PathVariable String packageNo) {
        EvidencePackageResponse response = evidencePackageService.getEvidencePackageByNo(packageNo);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 根据ID查询证据包
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EvidencePackageResponse>> getEvidencePackage(@PathVariable Long id) {
        EvidencePackageResponse response = evidencePackageService.getEvidencePackageByNo(id.toString());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 验证证据包
     */
    @PostMapping("/{id}/verify")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyEvidencePackage(@PathVariable Long id) {
        boolean verified = evidencePackageService.verifyEvidencePackage(id);
        return ResponseEntity.ok(ApiResponse.success(Map.of("verified", verified)));
    }

}