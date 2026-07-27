package com.example.contract.controller;

import com.example.contract.dto.request.ContractCreateRequest;
import com.example.contract.dto.response.ApiResponse;
import com.example.contract.dto.response.ContractResponse;
import com.example.contract.service.ContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 合同控制器
 */
@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    /**
     * 创建合同
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ContractResponse>> createContract(@Valid @RequestBody ContractCreateRequest request) {
        ContractResponse response = contractService.createContract(request);
        return ResponseEntity.ok(ApiResponse.success("创建成功", response));
    }

    /**
     * 查询合同列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ContractResponse>>> listContracts(
            @RequestParam(required = false) String status) {
        List<ContractResponse> response;
        if (status != null && !status.isEmpty()) {
            response = contractService.getContractsByStatus(status);
        } else {
            response = contractService.getAllContracts();
        }
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 根据合同编号查询合同
     */
    @GetMapping("/by-no/{contractNo}")
    public ResponseEntity<ApiResponse<ContractResponse>> getContractByNo(@PathVariable String contractNo) {
        ContractResponse response = contractService.getContractByNo(contractNo);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 根据ID查询合同
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContractResponse>> getContract(@PathVariable Long id) {
        ContractResponse response = contractService.getContractById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 更新合同
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ContractResponse>> updateContract(
            @PathVariable Long id,
            @Valid @RequestBody ContractCreateRequest request) {
        ContractResponse response = contractService.updateContract(id, request);
        return ResponseEntity.ok(ApiResponse.success("更新成功", response));
    }

    /**
     * 删除合同
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteContract(@PathVariable Long id) {
        contractService.deleteContract(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

}