package com.example.contract.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 跨链协同记录实体
 * 记录与外部司法链的交互记录，支持证据链追溯验证
 */
@Entity
@Table(name = "cross_chain_record", indexes = {
    @Index(name = "idx_cc_contract_id", columnList = "contract_id"),
    @Index(name = "idx_cc_target_chain", columnList = "target_chain"),
    @Index(name = "idx_cc_created", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrossChainRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联合同ID
     */
    @Column(nullable = false)
    private Long contractId;

    /**
     * 跨链类型：QUERY(查询), SYNC(同步), VERIFY(验证), EVIDENCE(证据传递)
     */
    @Column(nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private CrossChainType type;

    /**
     * 目标链类型：MARKET(市场监管链), COPYRIGHT(版权保护链), NOTARY(公证处存证链), COURT(法院司法链), OTHER(其他)
     */
    @Column(nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private TargetChain targetChain;

    /**
     * 目标链节点地址
     */
    @Column(length = 255)
    private String targetNodeUrl;

    /**
     * 请求数据（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String requestData;

    /**
     * 响应数据（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String responseData;

    /**
     * 跨链交易哈希
     */
    @Column(length = 64)
    private String crossChainTxHash;

    /**
     * 目标链存证ID
     */
    @Column(length = 64)
    private String targetEvidenceId;

    /**
     * 状态：PENDING(处理中), SUCCESS(成功), FAILED(失败), TIMEOUT(超时)
     */
    @Column(nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private CrossChainStatus status;

    /**
     * 错误信息
     */
    @Column(length = 512)
    private String errorMessage;

    /**
     * 重试次数
     */
    @Column(nullable = false)
    private Integer retryCount;

    /**
     * 创建时间
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 完成时间
     */
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (retryCount == null) {
            retryCount = 0;
        }
    }

    /**
     * 跨链类型枚举
     */
    public enum CrossChainType {
        QUERY,         // 查询
        SYNC,          // 同步
        VERIFY,        // 验证
        EVIDENCE       // 证据传递
    }

    /**
     * 目标链类型枚举
     */
    public enum TargetChain {
        MARKET,        // 市场监管链
        COPYRIGHT,     // 版权保护链
        NOTARY,        // 公证处存证链
        COURT,         // 法院司法链
        OTHER          // 其他
    }

    /**
     * 跨链状态枚举
     */
    public enum CrossChainStatus {
        PENDING,       // 处理中
        SUCCESS,       // 成功
        FAILED,        // 失败
        TIMEOUT        // 超时
    }
}