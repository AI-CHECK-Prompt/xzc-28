package com.example.contract.entity;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "workflow_node_connection")
public class WorkflowNodeConnection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long templateVersionId;
    private String sourceNodeId;
    private String targetNodeId;
    private String connectionType;  // DEFAULT, CONDITION_TRUE, CONDITION_FALSE
    private String conditionExpression;  // 条件表达式
}