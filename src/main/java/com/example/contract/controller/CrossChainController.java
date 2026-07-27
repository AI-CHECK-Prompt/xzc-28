package com.example.contract.controller;

import com.example.contract.dto.response.ApiResponse;
import com.example.contract.service.CrossChainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 跨链协同控制器
 */
@RestController
@RequestMapping("/api/cross-chain")
@RequiredArgsConstructor
public class CrossChainController {

    private final CrossChainService crossChainService;

    /**
     * 同步证据到外部链
     */
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<Map<String, Object>>> syncToExternalChain(
            @RequestParam Long contractId,
            @RequestParam String targetChain) {
        String crossTxHash = crossChainService.syncToExternalChain(contractId, targetChain);
        return ResponseEntity.ok(ApiResponse.success(Map.of("crossTxHash", crossTxHash)));
    }

    /**
     * 从外部链查询证据
     */
    @GetMapping("/query")
    public ResponseEntity<ApiResponse<Map<String, Object>>> queryFromExternalChain(
            @RequestParam Long contractId,
            @RequestParam String targetChain) {
        Map<String, Object> response = crossChainService.queryFromExternalChain(contractId, targetChain);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 验证跨链证据
     */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyCrossChainEvidence(
            @RequestParam Long contractId,
            @RequestParam String targetChain) {
        boolean verified = crossChainService.verifyCrossChainEvidence(contractId, targetChain);
        return ResponseEntity.ok(ApiResponse.success(Map.of("verified", verified)));
    }

    /**
     * 获取合同的跨链记录
     */
    @GetMapping("/records/{contractId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCrossChainRecords(@PathVariable Long contractId) {
        List<Map<String, Object>> records = crossChainService.getCrossChainRecords(contractId);
        return ResponseEntity.ok(ApiResponse.success(records));
    }

}