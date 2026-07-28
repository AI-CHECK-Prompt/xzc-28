package com.example.contract.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "workflow_execution_log")
public class WorkflowExecutionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long instanceId;
    private String nodeId;
    private String eventType;  // NODE_ENTERED, NODE_COMPLETED, TIMEOUT, CONDITION_EVALUATED
    private LocalDateTime eventTime;
    private String eventData;  // JSON格式事件数据
    private String operator;
}