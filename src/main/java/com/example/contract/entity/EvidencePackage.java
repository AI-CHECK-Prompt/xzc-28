package com.example.contract.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 证据包实体
 * 用于存储案件审理所需的证据目录和证据包信息
 */
@Entity
@Table(name = "evidence_package", indexes = {
    @Index(name = "idx_ep_contract_id", columnList = "contract_id"),
    @Index(name = "idx_ep_case_no", columnList = "case_no"),
    @Index(name = "idx_ep_created", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvidencePackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 证据包编号
     */
    @Column(nullable = false, unique = true, length = 64)
    private String packageNo;

    /**
     * 关联合同ID
     */
    @Column(nullable = false)
    private Long contractId;

    /**
     * 案件编号（法院系统）
     */
    @Column(length = 64)
    private String caseNo;

    /**
     * 证据包类型：NORMAL(普通), LEGAL(司法), ARBITRATION(仲裁)
     */
    @Column(nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private PackageType type;

    /**
     * 证据包状态：GENERATING(生成中), COMPLETED(已完成), VALIDATED(已验证), EXPIRED(已过期)
     */
    @Column(nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private PackageStatus status;

    /**
     * 证据目录JSON
     */
    @Column(columnDefinition = "TEXT")
    private String evidenceCatalog;

    /**
     * 证据包文件路径
     */
    @Column(length = 512)
    private String packagePath;

    /**
     * 证据包哈希值
     */
    @Column(length = 64)
    private String packageHash;

    /**
     * 证据可信度评分（综合评分）
     */
    @Column(nullable = false)
    private Integer credibilityScore;

    /**
     * 链上验证状态：NOT_VERIFIED(未验证), VERIFIED(已验证), FAILED(验证失败)
     */
    @Column(nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private ChainVerifyStatus chainVerifyStatus;

    /**
     * 验证时间
     */
    private LocalDateTime verifiedTime;

    /**
     * 生成时间
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 有效期至
     */
    private LocalDateTime expireTime;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (chainVerifyStatus == null) {
            chainVerifyStatus = ChainVerifyStatus.NOT_VERIFIED;
        }
    }

    /**
     * 证据包类型枚举
     */
    public enum PackageType {
        NORMAL,        // 普通证据包
        LEGAL,         // 司法证据包（符合法院要求）
        ARBITRATION    // 仲裁证据包
    }

    /**
     * 证据包状态枚举
     */
    public enum PackageStatus {
        GENERATING,    // 生成中
        COMPLETED,     // 已完成
        VALIDATED,     // 已验证
        EXPIRED        // 已过期
    }

    /**
     * 链上验证状态枚举
     */
    public enum ChainVerifyStatus {
        NOT_VERIFIED,  // 未验证
        VERIFIED,      // 已验证
        FAILED         // 验证失败
    }
}