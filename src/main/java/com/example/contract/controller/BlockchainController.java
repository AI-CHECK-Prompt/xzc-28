package com.example.contract.controller;

import com.example.contract.dto.response.ApiResponse;
import com.example.contract.service.BlockchainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 区块链控制器
 */
@RestController
@RequestMapping("/api/blockchain")
@RequiredArgsConstructor
public class BlockchainController {

    private final BlockchainService blockchainService;

    /**
     * 存储证据到联盟链
     */
    @PostMapping("/evidence/store")
    public ResponseEntity<ApiResponse<Map<String, Object>>> storeEvidence(
            @RequestParam Long contractId,
            @RequestParam(required = false) String hash) {
        String txHash = blockchainService.storeEvidence(hash != null ? hash : "test-hash", contractId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("txHash", txHash)));
    }

    /**
     * 验证链上证据
     */
    @PostMapping("/evidence/verify")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyEvidence(
            @RequestParam String txHash,
            @RequestParam String expectedHash) {
        boolean verified = blockchainService.verifyEvidence(txHash, expectedHash);
        return ResponseEntity.ok(ApiResponse.success(Map.of("verified", verified)));
    }

    /**
     * 存储证据到公有链
     */
    @PostMapping("/evidence/store-eth")
    public ResponseEntity<ApiResponse<Map<String, Object>>> storeEvidenceToEth(
            @RequestParam Long contractId,
            @RequestParam(required = false) String hash) {
        String txHash = blockchainService.storeEvidenceToEth(hash != null ? hash : "test-hash", contractId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("txHash", txHash)));
    }

    /**
     * 批量上链
     */
    @PostMapping("/evidence/batch/{contractId}")
    public ResponseEntity<ApiResponse<Void>> batchStoreEvidence(@PathVariable Long contractId) {
        blockchainService.batchStoreEvidence(contractId);
        return ResponseEntity.ok(ApiResponse.success("批量上链完成", null));
    }

}