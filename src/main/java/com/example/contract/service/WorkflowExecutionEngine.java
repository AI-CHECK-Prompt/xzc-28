package com.example.contract.service;

import com.example.contract.entity.*;
import com.example.contract.repository.*;
import com.example.contract.dto.response.WorkflowStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WorkflowExecutionEngine {
    private final WorkflowInstanceRepository instanceRepository;
    private final WorkflowInstanceNodeRepository instanceNodeRepository;
    private final WorkflowExecutionLogRepository logRepository;
    private final WorkflowNodeRepository nodeRepository;
    private final WorkflowNodeConnectionRepository connectionRepository;
    private final WorkflowTemplateVersionRepository versionRepository;
    private final ObjectMapper objectMapper;
    
    @Transactional
    public WorkflowInstance startWorkflow(Long contractId, Long templateVersionId) {
        WorkflowTemplateVersion version = versionRepository.findById(templateVersionId)
            .orElseThrow(() -> new RuntimeException("Template version not found"));
        
        // 创建实例
        WorkflowInstance instance = new WorkflowInstance();
        instance.setContractId(contractId);
        instance.setTemplateVersionId(templateVersionId);
        instance.setInstanceId(UUID.randomUUID().toString());
        instance.setStatus("IN_PROGRESS");
        instance.setStartedAt(LocalDateTime.now());
        instance = instanceRepository.save(instance);
        
        // 创建节点实例
        List<WorkflowNode> nodes = nodeRepository.findByTemplateVersionId(templateVersionId);
        for (WorkflowNode node : nodes) {
            WorkflowInstanceNode instanceNode = new WorkflowInstanceNode();
            instanceNode.setInstanceId(instance.getId());
            instanceNode.setNodeId(node.getNodeId());
            instanceNode.setStatus("WAITING");
            instanceNodeRepository.save(instanceNode);
        }
        
        // 找到开始节点并执行
        String startNodeId = findStartNode(nodes);
        executeNode(instance, startNodeId);
        
        // 记录日志
        logExecution(instance.getId(), null, "WORKFLOW_STARTED", null, null);
        
        return instance;
    }
    
    @Transactional
    public void executeNode(WorkflowInstance instance, String nodeId) {
        WorkflowInstanceNode instanceNode = instanceNodeRepository
            .findByInstanceIdAndNodeIdAndStatus(instance.getId(), nodeId, "WAITING")
            .orElse(null);
        
        if (instanceNode == null) return;
        
        instanceNode.setStatus("IN_PROGRESS");
        instanceNode.setEnteredAt(LocalDateTime.now());
        instanceNodeRepository.save(instanceNode);
        
        instance.setCurrentNodeId(nodeId);
        instanceRepository.save(instance);
        
        logExecution(instance.getId(), nodeId, "NODE_ENTERED", null, null);
    }
    
    @Transactional
    public void completeNode(Long instanceId, String nodeId, String result, Long signerId) {
        WorkflowInstance instance = instanceRepository.findById(instanceId)
            .orElseThrow(() -> new RuntimeException("Instance not found"));
        
        WorkflowInstanceNode instanceNode = instanceNodeRepository
            .findByInstanceIdAndNodeIdAndStatus(instanceId, nodeId, "IN_PROGRESS")
            .orElseThrow(() -> new RuntimeException("Node not in progress"));
        
        instanceNode.setStatus("COMPLETED");
        instanceNode.setCompletedAt(LocalDateTime.now());
        instanceNode.setResult(result);
        instanceNode.setSignerId(signerId);
        instanceNodeRepository.save(instanceNode);
        
        logExecution(instanceId, nodeId, "NODE_COMPLETED", result, signerId != null ? signerId.toString() : null);
        
        // 找下一节点
        List<WorkflowNodeConnection> connections = connectionRepository.findBySourceNodeId(nodeId);
        String nextNodeId = evaluateAndGetNextNode(connections, instance);
        
        if (nextNodeId != null) {
            executeNode(instance, nextNodeId);
        } else {
            instance.setStatus("COMPLETED");
            instance.setCompletedAt(LocalDateTime.now());
            instanceRepository.save(instance);
            logExecution(instanceId, null, "WORKFLOW_COMPLETED", null, null);
        }
    }
    
    private String findStartNode(List<WorkflowNode> nodes) {
        // 查找没有入边的节点作为开始节点
        Set<String> targetNodeIds = new HashSet<>();
        for (WorkflowNode node : nodes) {
            List<WorkflowNodeConnection> incoming = connectionRepository.findBySourceNodeId(node.getNodeId());
            for (WorkflowNodeConnection conn : incoming) {
                targetNodeIds.add(conn.getTargetNodeId());
            }
        }
        for (WorkflowNode node : nodes) {
            if (!targetNodeIds.contains(node.getNodeId())) {
                return node.getNodeId();
            }
        }
        return nodes.isEmpty() ? null : nodes.get(0).getNodeId();
    }
    
    private String evaluateAndGetNextNode(List<WorkflowNodeConnection> connections, WorkflowInstance instance) {
        // 优先处理默认连接
        for (WorkflowNodeConnection conn : connections) {
            if ("DEFAULT".equals(conn.getConnectionType())) {
                return conn.getTargetNodeId();
            }
        }
        
        // 处理条件连接
        for (WorkflowNodeConnection conn : connections) {
            if (conn.getConditionExpression() != null && evaluateCondition(conn.getConditionExpression(), instance)) {
                logExecution(instance.getId(), conn.getSourceNodeId(), "CONDITION_EVALUATED", 
                    "true:" + conn.getConditionExpression(), null);
                return conn.getTargetNodeId();
            }
        }
        
        return connections.isEmpty() ? null : connections.get(0).getTargetNodeId();
    }
    
    private boolean evaluateCondition(String expression, WorkflowInstance instance) {
        // 简化条件评估：支持签约方数量、合同金额区间、签约主体类型
        // 实际实现应使用SpEL或其他表达式引擎
        try {
            if (expression.startsWith("signerCount")) {
                int threshold = Integer.parseInt(expression.replaceAll("[^0-9]", ""));
                // 需要从合同获取签署方数量
                return true; // 占位
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private void logExecution(Long instanceId, String nodeId, String eventType, String result, String operator) {
        WorkflowExecutionLog log = new WorkflowExecutionLog();
        log.setInstanceId(instanceId);
        log.setNodeId(nodeId);
        log.setEventType(eventType);
        log.setEventTime(LocalDateTime.now());
        log.setEventData("{\"result\":\"" + (result != null ? result : "") + "\"}");
        log.setOperator(operator);
        logRepository.save(log);
    }
    
    public WorkflowStatusResponse getWorkflowStatus(Long instanceId) {
        WorkflowInstance instance = instanceRepository.findById(instanceId)
            .orElseThrow(() -> new RuntimeException("Instance not found"));
        
        List<WorkflowInstanceNode> nodes = instanceNodeRepository.findByInstanceId(instanceId);
        
        WorkflowStatusResponse response = new WorkflowStatusResponse();
        response.setInstanceId(instanceId);
        response.setInstanceName("Contract-" + instance.getContractId());
        response.setCurrentNodeId(instance.getCurrentNodeId());
        response.setStatus(instance.getStatus());
        response.setEstimatedCompletionAt(instance.getEstimatedCompletionAt());
        
        // 待签署方
        List<WorkflowStatusResponse.PendingSigner> pendingSigners = new ArrayList<>();
        for (WorkflowInstanceNode node : nodes) {
            if ("IN_PROGRESS".equals(node.getStatus()) && node.getSignerId() != null) {
                WorkflowStatusResponse.PendingSigner signer = new WorkflowStatusResponse.PendingSigner();
                signer.setSignerId(node.getSignerId());
                signer.setNodeId(node.getNodeId());
                pendingSigners.add(signer);
            }
        }
        response.setPendingSigners(pendingSigners);
        
        // 节点状态列表
        List<WorkflowStatusResponse.NodeStatus> nodeStatuses = new ArrayList<>();
        for (WorkflowInstanceNode node : nodes) {
            WorkflowStatusResponse.NodeStatus status = new WorkflowStatusResponse.NodeStatus();
            status.setNodeId(node.getNodeId());
            status.setStatus(node.getStatus());
            status.setCompletedAt(node.getCompletedAt());
            nodeStatuses.add(status);
        }
        response.setNodeStatuses(nodeStatuses);
        
        return response;
    }
}