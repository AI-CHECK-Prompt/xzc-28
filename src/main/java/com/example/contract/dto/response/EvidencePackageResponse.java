package com.example.contract.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 证据包响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvidencePackageResponse {

    /**
     * 证据包ID
     */
    private Long id;

    /**
     * 证据包编号
     */
    private String packageNo;

    /**
     * 关联合同信息
     */
    private ContractResponse contract;

    /**
     * 案件编号
     */
    private String caseNo;

    /**
     * 证据包类型
     */
    private String type;

    /**
     * 证据包状态
     */
    private String status;

    /**
     * 证据目录JSON
     */
    private String evidenceCatalog;

    /**
     * 证据包哈希值
     */
    private String packageHash;

    /**
     * 证据可信度评分
     */
    private Integer credibilityScore;

    /**
     * 链上验证状态
     */
    private String chainVerifyStatus;

    /**
     * 验证时间
     */
    private LocalDateTime verifiedTime;

    /**
     * 生成时间
     */
    private LocalDateTime createdAt;

    /**
     * 有效期至
     */
    private LocalDateTime expireTime;

}