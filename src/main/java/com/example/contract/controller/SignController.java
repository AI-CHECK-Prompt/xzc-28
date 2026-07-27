package com.example.contract.controller;

import com.example.contract.dto.request.SignRequest;
import com.example.contract.dto.response.ApiResponse;
import com.example.contract.dto.response.SignRecordResponse;
import com.example.contract.service.SignService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 签署控制器
 */
@RestController
@RequestMapping("/api/sign")
@RequiredArgsConstructor
public class SignController {

    private final SignService signService;

    /**
     * 执行签署
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SignRecordResponse>> sign(@Valid @RequestBody SignRequest request) {
        SignRecordResponse response = signService.sign(request);
        return ResponseEntity.ok(ApiResponse.success("签署成功", response));
    }

    /**
     * 查询合同签署记录
     */
    @GetMapping("/contract/{contractId}")
    public ResponseEntity<ApiResponse<List<SignRecordResponse>>> getSignRecords(@PathVariable Long contractId) {
        List<SignRecordResponse> response = signService.getSignRecords(contractId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 根据ID查询签署记录
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SignRecordResponse>> getSignRecord(@PathVariable Long id) {
        SignRecordResponse response = signService.getSignRecordById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

}