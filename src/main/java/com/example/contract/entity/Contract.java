package com.example.contract.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 合同实体
 * 存储电子合同的基本信息和签署状态
 */
@Entity
@Table(name = "contract", indexes = {
    @Index(name = "idx_contract_no", columnList = "contract_no"),
    @Index(name = "idx_contract_status", columnList = "status"),
    @Index(name = "idx_contract_created", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 合同编号（唯一标识）
     */
    @Column(nullable = false, unique = true, length = 64)
    private String contractNo;

    /**
     * 合同名称
     */
    @Column(nullable = false, length = 255)
    private String name;

    /**
     * 合同类型：SERVICE(服务合同), SALES(销售合同), LEASE(租赁合同), EMPLOYMENT(劳动合同), OTHER(其他)
     */
    @Column(nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private ContractType type;

    /**
     * 合同状态：DRAFT(草稿), PENDING(待签署), PARTIAL(部分签署), SIGNED(已签署), REVOKED(已撤销), EXPIRED(已过期)
     */
    @Column(nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private ContractStatus status;

    /**
     * 合同原文指纹（SHA-256哈希值）
     */
    @Column(nullable = false, length = 64)
    private String contentHash;

    /**
     * 合同文件存储路径
     */
    @Column(length = 512)
    private String filePath;

    /**
     * 合同文件大小（字节）
     */
    private Long fileSize;

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
     * 链上交易哈希（联盟链）
     */
    @Column(length = 64)
    private String fabricTxHash;

    /**
     * 链上交易哈希（公有链）
     */
    @Column(length = 64)
    private String ethTxHash;

    /**
     * 存证状态：PENDING(待存证), SUCCESS(存证成功), FAILED(存证失败)
     */
    @Column(nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private EvidenceStatus evidenceStatus;

    /**
     * 证据可信度评分（0-100）
     */
    private Integer credibilityScore;

    /**
     * 创建人ID
     */
    @Column(nullable = false)
    private Long creatorId;

    /**
     * 创建时间
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 最后签署时间
     */
    private LocalDateTime lastSignTime;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (evidenceStatus == null) {
            evidenceStatus = EvidenceStatus.PENDING;
        }
        if (status == null) {
            status = ContractStatus.DRAFT;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 合同类型枚举
     */
    public enum ContractType {
        SERVICE,       // 服务合同
        SALES,         // 销售合同
        LEASE,         // 租赁合同
        EMPLOYMENT,    // 劳动合同
        OTHER          // 其他
    }

    /**
     * 合同状态枚举
     */
    public enum ContractStatus {
        DRAFT,         // 草稿
        PENDING,       // 待签署
        PARTIAL,       // 部分签署
        SIGNED,        // 已签署
        REVOKED,       // 已撤销
        EXPIRED        // 已过期
    }

    /**
     * 存证状态枚举
     */
    public enum EvidenceStatus {
        PENDING,       // 待存证
        SUCCESS,       // 存证成功
        FAILED         // 存证失败
    }
}