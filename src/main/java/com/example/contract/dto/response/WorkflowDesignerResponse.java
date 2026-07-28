package com.example.contract.dto.response;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class WorkflowDesignerResponse {
    private Long templateVersionId;
    private String bpmnXml;
    private List<NodeInfo> nodes;
    private List<ConnectionInfo> connections;
    
    @Data
    public static class NodeInfo {
        private String nodeId;
        private String nodeType;
        private String nodeName;
        private Integer positionX;
        private Integer positionY;
        private Map<String, Object> config;
    }
    
    @Data
    public static class ConnectionInfo {
        private String sourceNodeId;
        private String targetNodeId;
        private String connectionType;
        private String conditionExpression;
    }
}