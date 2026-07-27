package com.example.contract.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 合同响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractResponse {

    /**
     * 合同ID
     */
    private Long id;

    /**
     * 合同编号
     */
    private String contractNo;

    /**
     * 合同名称
     */
    private String name;

    /**
     * 合同类型
     */
    private String type;

    /**
     * 合同状态
     */
    private String status;

    /**
     * 合同原文指纹
     */
    private String contentHash;

    /**
     * 签署截止时间
     */
    private LocalDateTime signDeadline;

    /**
     * 合同生效时间
     */
    private LocalDateTime effectiveTime;

    /**
     * 合同到期时间
     */
    private LocalDateTime expireTime;

    /**
     * 联盟链交易哈希
     */
    private String fabricTxHash;

    /**
     * 公有链交易哈希
     */
    private String ethTxHash;

    /**
     * 存证状态
     */
    private String evidenceStatus;

    /**
     * 证据可信度评分
     */
    private Integer credibilityScore;

    /**
     * 签署方列表
     */
    private List<SignerResponse> signers;

    /**
     * 签署记录列表
     */
    private List<SignRecordResponse> signRecords;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 最后签署时间
     */
    private LocalDateTime lastSignTime;

}