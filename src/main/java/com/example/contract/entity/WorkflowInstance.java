package com.example.contract.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "workflow_instance")
public class WorkflowInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long contractId;
    private Long templateVersionId;  // 锁定模板版本
    private String instanceId;  // 流程实例唯一标识
    private String currentNodeId;  // 当前节点
    private String status;  // PENDING, IN_PROGRESS, COMPLETED, CANCELLED, TIMEOUT
    private LocalDateTime startedAt;
    private LocalDateTime estimatedCompletionAt;
    private LocalDateTime completedAt;
}