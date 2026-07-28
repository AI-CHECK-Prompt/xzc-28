package com.example.contract.service;

import com.example.contract.entity.*;
import com.example.contract.repository.*;
import com.example.contract.dto.request.WorkflowTemplateRequest;
import com.example.contract.dto.response.WorkflowDesignerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WorkflowDesignerService {
    private final WorkflowTemplateRepository templateRepository;
    private final WorkflowTemplateVersionRepository versionRepository;
    private final WorkflowNodeRepository nodeRepository;
    private final WorkflowNodeConnectionRepository connectionRepository;
    private final ObjectMapper objectMapper;
    
    public WorkflowDesignerResponse getDesignerData(Long templateVersionId) {
        WorkflowTemplateVersion version = versionRepository.findById(templateVersionId)
            .orElseThrow(() -> new RuntimeException("Template version not found"));
        
        List<WorkflowNode> nodes = nodeRepository.findByTemplateVersionId(templateVersionId);
        List<WorkflowNodeConnection> connections = connectionRepository.findByTemplateVersionId(templateVersionId);
        
        WorkflowDesignerResponse response = new WorkflowDesignerResponse();
        response.setTemplateVersionId(templateVersionId);
        response.setBpmnXml(version.getBpmnXml());
        
        // 转换节点
        List<WorkflowDesignerResponse.NodeInfo> nodeInfos = new ArrayList<>();
        for (WorkflowNode node : nodes) {
            WorkflowDesignerResponse.NodeInfo info = new WorkflowDesignerResponse.NodeInfo();
            info.setNodeId(node.getNodeId());
            info.setNodeType(node.getNodeType());
            info.setNodeName(node.getNodeName());
            info.setPositionX(node.getPositionX());
            info.setPositionY(node.getPositionY());
            try {
                info.setConfig(objectMapper.readValue(node.getConfigJson(), Map.class));
            } catch (Exception e) {
                info.setConfig(new HashMap<>());
            }
            nodeInfos.add(info);
        }
        response.setNodes(nodeInfos);
        
        // 转换连接
        List<WorkflowDesignerResponse.ConnectionInfo> connectionInfos = new ArrayList<>();
        for (WorkflowNodeConnection conn : connections) {
            WorkflowDesignerResponse.ConnectionInfo info = new WorkflowDesignerResponse.ConnectionInfo();
            info.setSourceNodeId(conn.getSourceNodeId());
            info.setTargetNodeId(conn.getTargetNodeId());
            info.setConnectionType(conn.getConnectionType());
            info.setConditionExpression(conn.getConditionExpression());
            connectionInfos.add(info);
        }
        response.setConnections(connectionInfos);
        
        return response;
    }
    
    @Transactional
    public void saveDesignerData(Long templateVersionId, WorkflowDesignerResponse data) {
        // 保存节点
        for (WorkflowDesignerResponse.NodeInfo nodeInfo : data.getNodes()) {
            WorkflowNode node = new WorkflowNode();
            node.setTemplateVersionId(templateVersionId);
            node.setNodeId(nodeInfo.getNodeId());
            node.setNodeType(nodeInfo.getNodeType());
            node.setNodeName(nodeInfo.getNodeName());
            node.setPositionX(nodeInfo.getPositionX());
            node.setPositionY(nodeInfo.getPositionY());
            try {
                node.setConfigJson(objectMapper.writeValueAsString(nodeInfo.getConfig()));
            } catch (Exception e) {}
            nodeRepository.save(node);
        }
        
        // 保存连接
        for (WorkflowDesignerResponse.ConnectionInfo connInfo : data.getConnections()) {
            WorkflowNodeConnection conn = new WorkflowNodeConnection();
            conn.setTemplateVersionId(templateVersionId);
            conn.setSourceNodeId(connInfo.getSourceNodeId());
            conn.setTargetNodeId(connInfo.getTargetNodeId());
            conn.setConnectionType(connInfo.getConnectionType());
            conn.setConditionExpression(connInfo.getConditionExpression());
            connectionRepository.save(conn);
        }
    }
}