package com.example.contract.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class WorkflowStatusResponse {
    private Long instanceId;
    private String instanceName;
    private String currentNodeId;
    private String currentNodeName;
    private String status;
    private List<PendingSigner> pendingSigners;
    private LocalDateTime estimatedCompletionAt;
    private List<NodeStatus> nodeStatuses;
    
    @Data
    public static class PendingSigner {
        private Long signerId;
        private String signerName;
        private String nodeId;
    }
    
    @Data
    public static class NodeStatus {
        private String nodeId;
        private String nodeName;
        private String status;
        private LocalDateTime completedAt;
    }
}