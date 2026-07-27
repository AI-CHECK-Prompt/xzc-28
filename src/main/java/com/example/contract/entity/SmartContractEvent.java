package com.example.contract.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 智能合约事件实体
 * 记录智能合约执行的事件，包括存证费用结算、异常预警等
 */
@Entity
@Table(name = "smart_contract_event", indexes = {
    @Index(name = "idx_sc_contract_id", columnList = "contract_id"),
    @Index(name = "idx_sc_event_type", columnList = "event_type"),
    @Index(name = "idx_sc_created", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmartContractEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联合同ID
     */
    private Long contractId;

    /**
     * 事件类型：SETTLEMENT(费用结算), WARNING(异常预警), EVIDENCE_ASSEMBLY(证据包组装), SIGN_VERIFY(签名验证), OTHER(其他)
     */
    @Column(nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    /**
     * 事件级别：INFO(信息), WARNING(警告), ERROR(错误), CRITICAL(严重)
     */
    @Column(nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private EventLevel level;

    /**
     * 事件标题
     */
    @Column(nullable = false, length = 255)
    private String title;

    /**
     * 事件描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 链上交易哈希
     */
    @Column(length = 64)
    private String txHash;

    /**
     * 合约地址
     */
    @Column(length = 64)
    private String contractAddress;

    /**
     * 事件数据（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String eventData;

    /**
     * 处理状态：PENDING(待处理), PROCESSING(处理中), RESOLVED(已解决), IGNORED(已忽略)
     */
    @Column(nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private ProcessStatus processStatus;

    /**
     * 创建时间
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 处理时间
     */
    private LocalDateTime processedAt;

    /**
     * 处理人ID
     */
    private Long processedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (processStatus == null) {
            processStatus = ProcessStatus.PENDING;
        }
    }

    /**
     * 事件类型枚举
     */
    public enum EventType {
        SETTLEMENT,          // 费用结算
        WARNING,             // 异常预警
        EVIDENCE_ASSEMBLY,   // 证据包组装
        SIGN_VERIFY,         // 签名验证
        OTHER                // 其他
    }

    /**
     * 事件级别枚举
     */
    public enum EventLevel {
        INFO,                // 信息
        WARNING,             // 警告
        ERROR,               // 错误
        CRITICAL             // 严重
    }

    /**
     * 处理状态枚举
     */
    public enum ProcessStatus {
        PENDING,             // 待处理
        PROCESSING,          // 处理中
        RESOLVED,            // 已解决
        IGNORED              // 已忽略
    }
}