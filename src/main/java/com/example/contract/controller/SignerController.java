package com.example.contract.controller;

import com.example.contract.dto.request.SignerCreateRequest;
import com.example.contract.dto.response.ApiResponse;
import com.example.contract.dto.response.SignerResponse;
import com.example.contract.service.SignerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 签约方控制器
 */
@RestController
@RequestMapping("/api/signers")
@RequiredArgsConstructor
public class SignerController {

    private final SignerService signerService;

    /**
     * 创建签约方
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SignerResponse>> createSigner(@Valid @RequestBody SignerCreateRequest request) {
        SignerResponse response = signerService.createSigner(request);
        return ResponseEntity.ok(ApiResponse.success("创建成功", response));
    }

    /**
     * 查询签约方列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SignerResponse>>> listSigners(
            @RequestParam(required = false) String type) {
        List<SignerResponse> response;
        if (type != null && !type.isEmpty()) {
            response = signerService.getSignersByType(type);
        } else {
            response = signerService.getAllSigners();
        }
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 根据ID查询签约方
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SignerResponse>> getSigner(@PathVariable Long id) {
        SignerResponse response = signerService.getSignerById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 更新签约方信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SignerResponse>> updateSigner(
            @PathVariable Long id,
            @Valid @RequestBody SignerCreateRequest request) {
        SignerResponse response = signerService.updateSigner(id, request);
        return ResponseEntity.ok(ApiResponse.success("更新成功", response));
    }

    /**
     * 删除签约方
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSigner(@PathVariable Long id) {
        signerService.deleteSigner(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

}