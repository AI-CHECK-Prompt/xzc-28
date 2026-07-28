package com.example.contract.dto.request;

import lombok.Data;
@Data
public class WorkflowNodeRequest {
    private String nodeId;
    private String nodeType;
    private String nodeName;
    private Integer positionX;
    private Integer positionY;
    private String configJson;
    private Integer timeoutHours;
    private String timeoutAction;
}