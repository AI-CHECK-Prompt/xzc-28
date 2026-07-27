package com.example.contract.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 签署记录实体
 * 记录每次签署行为的详细信息，包括签署时间、设备环境、网络信息等
 */
@Entity
@Table(name = "sign_record", indexes = {
    @Index(name = "idx_sign_contract_id", columnList = "contract_id"),
    @Index(name = "idx_sign_signer_id", columnList = "signer_id"),
    @Index(name = "idx_sign_created", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联合同ID
     */
    @Column(nullable = false)
    private Long contractId;

    /**
     * 关联签约方ID
     */
    @Column(nullable = false)
    private Long signerId;

    /**
     * 签署顺序（第几个签署）
     */
    @Column(nullable = false)
    private Integer signOrder;

    /**
     * 签署类型：INITIAL(初始签署), COUNTERSIGN(会签), WITNESS(见证), NOTARY(公证)
     */
    @Column(nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private SignType signType;

    /**
     * 签署状态：PENDING(待签署), COMPLETED(已签署), REJECTED(拒绝签署)
     */
    @Column(nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private SignStatus status;

    /**
     * 签署时间戳（权威时间源）
     */
    @Column(nullable = false)
    private LocalDateTime signTime;

    /**
     * 时间戳权威来源：NTP(网络时间协议), GPS(卫星时间), CA(证书时间), BLOCKCHAIN(区块链时间)
     */
    @Column(length = 32)
    @Enumerated(EnumType.STRING)
    private TimeSource timeSource;

    /**
     * 签署行为哈希值
     */
    @Column(nullable = false, length = 64)
    private String actionHash;

    /**
     * 签署设备类型：PC, MOBILE, TABLET, SERVER
     */
    @Column(length = 32)
    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;

    /**
     * 设备型号
     */
    @Column(length = 128)
    private String deviceModel;

    /**
     * 操作系统
     */
    @Column(length = 64)
    private String os;

    /**
     * IP地址
     */
    @Column(length = 45)
    private String ipAddress;

    /**
     * 网络类型：WIFI, 4G, 5G, LAN
     */
    @Column(length = 32)
    private String networkType;

    /**
     * 地理位置（经纬度）
     */
    @Column(length = 64)
    private String location;

    /**
     * 浏览器信息
     */
    @Column(length = 255)
    private String userAgent;

    /**
     * 签名算法：RSA, ECDSA, SM2
     */
    @Column(length = 32)
    private String signatureAlgorithm;

    /**
     * 数字签名值
     */
    @Column(columnDefinition = "TEXT")
    private String signature;

    /**
     * 证书链
     */
    @Column(columnDefinition = "TEXT")
    private String certificateChain;

    /**
     * 链上交易哈希（联盟链）
     */
    @Column(length = 64)
    private String fabricTxHash;

    /**
     * 签署时签约方认证级别（快照，用于评分计算，避免时间窗口耦合）
     */
    @Column(length = 32)
    @Enumerated(EnumType.STRING)
    private Signer.AuthLevel signerAuthLevel;

    /**
     * 签署时签约方认证状态（快照，用于评分计算）
     */
    @Column(length = 32)
    @Enumerated(EnumType.STRING)
    private Signer.AuthStatus signerAuthStatus;

    /**
     * 创建时间
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * 签署类型枚举
     */
    public enum SignType {
        INITIAL,       // 初始签署
        COUNTERSIGN,   // 会签
        WITNESS,       // 见证
        NOTARY         // 公证
    }

    /**
     * 签署状态枚举
     */
    public enum SignStatus {
        PENDING,       // 待签署
        COMPLETED,     // 已签署
        REJECTED       // 拒绝签署
    }

    /**
     * 时间源枚举
     */
    public enum TimeSource {
        NTP,           // 网络时间协议
        GPS,           // 卫星时间
        CA,            // 证书时间
        BLOCKCHAIN     // 区块链时间
    }

    /**
     * 设备类型枚举
     */
    public enum DeviceType {
        PC,            // 电脑
        MOBILE,        // 手机
        TABLET,        // 平板
        SERVER         // 服务器
    }
}