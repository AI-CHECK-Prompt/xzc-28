package com.example.contract.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "workflow_instance_node")
public class WorkflowInstanceNode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long instanceId;
    private String nodeId;
    private String status;  // WAITING, IN_PROGRESS, COMPLETED, SKIPPED, TIMEOUT
    private Long signerId;  // 关联签署方
    private LocalDateTime enteredAt;
    private LocalDateTime completedAt;
    private String result;  // SIGNED, REJECTED, TIMEOUT_SKIPPED
}