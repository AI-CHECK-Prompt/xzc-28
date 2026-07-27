package com.example.contract.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 合约模板实体
 * 支持可视化拖拽编排的合约模板，包含签约流程分支逻辑配置
 */
@Entity
@Table(name = "contract_template", indexes = {
    @Index(name = "idx_template_code", columnList = "template_code"),
    @Index(name = "idx_template_status", columnList = "status"),
    @Index(name = "idx_template_created", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 模板编号
     */
    @Column(nullable = false, unique = true, length = 64)
    private String templateCode;

    /**
     * 模板名称
     */
    @Column(nullable = false, length = 255)
    private String name;

    /**
     * 模板描述
     */
    @Column(length = 1024)
    private String description;

    /**
     * 模板类型：STANDARD(标准模板), CUSTOM(自定义模板), DYNAMIC(动态模板)
     */
    @Column(nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private TemplateType type;

    /**
     * 模板状态：DRAFT(草稿), ACTIVE(启用), INACTIVE(停用)
     */
    @Column(nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private TemplateStatus status;

    /**
     * 模板内容（JSON格式，包含流程配置）
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * 流程定义（BPMN或自定义JSON）
     */
    @Column(columnDefinition = "TEXT")
    private String flowDefinition;

    /**
     * 条件触发规则（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String triggerRules;

    /**
     * 签署方配置（JSON格式，定义参与方角色）
     */
    @Column(columnDefinition = "TEXT")
    private String signerConfig;

    /**
     * 表单字段配置（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String formFields;

    /**
     * 默认签署截止时间（小时）
     */
    private Integer defaultDeadlineHours;

    /**
     * 是否自动归档
     */
    @Column(nullable = false)
    private Boolean autoArchive;

    /**
     * 是否需要公证
     */
    @Column(nullable = false)
    private Boolean needNotary;

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
     * 使用次数
     */
    @Column(nullable = false)
    private Integer usageCount;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (usageCount == null) {
            usageCount = 0;
        }
        if (autoArchive == null) {
            autoArchive = false;
        }
        if (needNotary == null) {
            needNotary = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 模板类型枚举
     */
    public enum TemplateType {
        STANDARD,      // 标准模板
        CUSTOM,        // 自定义模板
        DYNAMIC        // 动态模板（可配置流程）
    }

    /**
     * 模板状态枚举
     */
    public enum TemplateStatus {
        DRAFT,         // 草稿
        ACTIVE,        // 启用
        INACTIVE       // 停用
    }
}