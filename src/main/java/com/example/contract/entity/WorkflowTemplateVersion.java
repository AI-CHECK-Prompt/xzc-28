package com.example.contract.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "workflow_template_version")
public class WorkflowTemplateVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long templateId;
    private Integer version;
    private String bpmnXml;  // BPMN XML定义
    private String nodeConfigJson;  // 节点配置JSON
    private Boolean isActive;  // 是否为当前活跃版本
    private LocalDateTime createdAt;
    private String createdBy;
}