package com.example.contract.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 签约主体实体
 * 存储签约方身份信息，包括实名认证、证书信息等
 */
@Entity
@Table(name = "signer", indexes = {
    @Index(name = "idx_signer_id_card", columnList = "id_card"),
    @Index(name = "idx_signer_phone", columnList = "phone")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Signer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 签约方名称
     */
    @Column(nullable = false, length = 255)
    private String name;

    /**
     * 身份证号（个人）或统一社会信用代码（企业）
     */
    @Column(nullable = false, unique = true, length = 64)
    private String idCard;

    /**
     * 签约方类型：INDIVIDUAL(个人), ENTERPRISE(企业), ORGANIZATION(机构)
     */
    @Column(nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private SignerType type;

    /**
     * 手机号
     */
    @Column(length = 20)
    private String phone;

    /**
     * 邮箱
     */
    @Column(length = 128)
    private String email;

    /**
     * 实名认证状态
     */
    @Column(nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private AuthStatus authStatus;

    /**
     * 认证级别：LEVEL1(基础认证), LEVEL2(实名认证), LEVEL3(人脸核验), LEVEL4(CA证书)
     */
    @Column(length = 32)
    @Enumerated(EnumType.STRING)
    private AuthLevel authLevel;

    /**
     * 数字证书序列号
     */
    @Column(length = 128)
    private String certSerialNumber;

    /**
     * 身份戳（链上存证标识）
     */
    @Column(length = 64)
    private String identityStamp;

    /**
     * 创建时间
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 签约方类型枚举
     */
    public enum SignerType {
        INDIVIDUAL,    // 个人
        ENTERPRISE,    // 企业
        ORGANIZATION   // 机构
    }

    /**
     * 认证状态枚举
     */
    public enum AuthStatus {
        UNVERIFIED,    // 未认证
        PENDING,       // 审核中
        VERIFIED,      // 已认证
        REJECTED       // 认证失败
    }

    /**
     * 认证级别枚举
     */
    public enum AuthLevel {
        LEVEL1,        // 基础认证（手机号验证）
        LEVEL2,        // 实名认证（身份证验证）
        LEVEL3,        // 人脸核验
        LEVEL4         // CA证书认证
    }
}