package com.example.contract.entity;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "workflow_node")
public class WorkflowNode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long templateVersionId;
    private String nodeId;  // 节点唯一标识
    private String nodeType;  // SIGN_SIGNER, CONDITION, PARALLEL_GROUP, TIMEOUT
    private String nodeName;
    private Integer positionX;
    private Integer positionY;
    private String configJson;  // 节点配置：签署方、签署方式、顺序权重等
    private Integer timeoutHours;  // 超时时间（小时）
    private String timeoutAction;  // AUTO_SKIP, REMIND, ESCALATE
}