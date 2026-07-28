# 签约流程可视化编排模块实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 为互联网法院合同管理系统新增签约流程可视化编排模块，支持拖拽式流程设计、条件分支、并行签署、超时处理、模板版本管理、实时状态展示和审计日志。

**Architecture:**
- 后端：Java Spring Boot，扩展现有contract模块
- 前端：Vue.js + Element UI，使用bpmn-js流程设计器
- 流程引擎：自研轻量级状态机引擎
- 持久化：JPA + MySQL

## 技术栈
- Java 8+, Spring Boot 2.x, JPA, MySQL
- Vue.js 3, Element Plus, bpmn-js
- Redis（实时状态缓存）

## 全局约束
- 模板版本管理：模板变更不影响已创建的合同
- 流程节点支持：签署节点、条件分支节点、并行组节点、超时处理节点
- 审计日志：记录所有签署动作和时间戳

---

## 文件结构

```
src/main/java/com/example/contract/
├── entity/
│   ├── WorkflowTemplate.java          # 流程模板
│   ├── WorkflowTemplateVersion.java   # 模板版本
│   ├── WorkflowNode.java             # 流程节点定义
│   ├── WorkflowNodeConnection.java   # 节点连线
│   ├── WorkflowInstance.java         # 流程实例
│   ├── WorkflowInstanceNode.java     # 实例节点状态
│   └── WorkflowExecutionLog.java      # 执行日志
├── repository/
│   ├── WorkflowTemplateRepository.java
│   ├── WorkflowTemplateVersionRepository.java
│   ├── WorkflowNodeRepository.java
│   ├── WorkflowNodeConnectionRepository.java
│   ├── WorkflowInstanceRepository.java
│   ├── WorkflowInstanceNodeRepository.java
│   └── WorkflowExecutionLogRepository.java
├── service/
│   ├── WorkflowTemplateService.java  # 模板CRUD+版本管理
│   ├── WorkflowDesignerService.java  # 流程设计器服务
│   └── WorkflowExecutionEngine.java  # 流程执行引擎
├── dto/
│   ├── request/
│   │   ├── WorkflowTemplateRequest.java
│   │   ├── WorkflowNodeRequest.java
│   │   └── WorkflowInstanceRequest.java
│   └── response/
│       ├── WorkflowTemplateResponse.java
│       ├── WorkflowDesignerResponse.java
│       └── WorkflowStatusResponse.java
└── controller/
    └── WorkflowController.java       # 流程设计器API

前端:
src/views/workflow/
├── designer.vue                      # 拖拽式流程设计器
├── template-list.vue                 # 模板列表
├── instance-list.vue                 # 实例列表
└── monitor.vue                       # 实时监控
```

---

## 任务清单

### Task 1: 实体类创建

**Files:**
- Create: `src/main/java/com/example/contract/entity/WorkflowTemplate.java`
- Create: `src/main/java/com/example/contract/entity/WorkflowTemplateVersion.java`
- Create: `src/main/java/com/example/contract/entity/WorkflowNode.java`
- Create: `src/main/java/com/example/contract/entity/WorkflowNodeConnection.java`
- Create: `src/main/java/com/example/contract/entity/WorkflowInstance.java`
- Create: `src/main/java/com/example/contract/entity/WorkflowInstanceNode.java`
- Create: `src/main/java/com/example/contract/entity/WorkflowExecutionLog.java`

**Interfaces:**
- Produces: 7个实体类，供后续任务使用

- [ ] **Step 1: 创建WorkflowTemplate.java**
```java
package com.example.contract.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "workflow_template")
public class WorkflowTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String description;
    private String category;  // 多方协议、三方合同等
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 2: 创建WorkflowTemplateVersion.java**
```java
package com.example.contract.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "workflow_template_version")
public class WorkflowTemplateVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long templateId;
    private Integer version;
    private String bpmnXml;  // BPMN XML定义
    private String nodeConfigJson;  // 节点配置JSON
    private Boolean isActive;  // 是否为当前活跃版本
    private LocalDateTime createdAt;
    private String createdBy;
}
```

- [ ] **Step 3: 创建WorkflowNode.java**
```java
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
```

- [ ] **Step 4: 创建WorkflowNodeConnection.java**
```java
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
```

- [ ] **Step 5: 创建WorkflowInstance.java**
```java
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
```

- [ ] **Step 6: 创建WorkflowInstanceNode.java**
```java
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
```

- [ ] **Step 7: 创建WorkflowExecutionLog.java**
```java
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
```

- [ ] **Step 8: 提交**
```bash
git add src/main/java/com/example/contract/entity/Workflow*.java
git commit -m "feat: add workflow entities"
```

---

### Task 2: Repository接口创建

**Files:**
- Create: `src/main/java/com/example/contract/repository/WorkflowTemplateRepository.java`
- Create: `src/main/java/com/example/contract/repository/WorkflowTemplateVersionRepository.java`
- Create: `src/main/java/com/example/contract/repository/WorkflowNodeRepository.java`
- Create: `src/main/java/com/example/contract/repository/WorkflowNodeConnectionRepository.java`
- Create: `src/main/java/com/example/contract/repository/WorkflowInstanceRepository.java`
- Create: `src/main/java/com/example/contract/repository/WorkflowInstanceNodeRepository.java`
- Create: `src/main/java/com/example/contract/repository/WorkflowExecutionLogRepository.java`

**Interfaces:**
- Consumes: 7个实体类
- Produces: JPA Repository接口

- [ ] **Step 1: 创建WorkflowTemplateRepository.java**
```java
package com.example.contract.repository;

import com.example.contract.entity.WorkflowTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WorkflowTemplateRepository extends JpaRepository<WorkflowTemplate, Long> {
    List<WorkflowTemplate> findByIsActiveTrue();
    List<WorkflowTemplate> findByCategory(String category);
}
```

- [ ] **Step 2: 创建WorkflowTemplateVersionRepository.java**
```java
package com.example.contract.repository;

import com.example.contract.entity.WorkflowTemplateVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WorkflowTemplateVersionRepository extends JpaRepository<WorkflowTemplateVersion, Long> {
    Optional<WorkflowTemplateVersion> findByTemplateIdAndIsActiveTrue(Long templateId);
    Optional<WorkflowTemplateVersion> findTopByTemplateIdOrderByVersionDesc(Long templateId);
}
```

- [ ] **Step 3: 创建其他Repository**
```java
// WorkflowNodeRepository.java
package com.example.contract.repository;

import com.example.contract.entity.WorkflowNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WorkflowNodeRepository extends JpaRepository<WorkflowNode, Long> {
    List<WorkflowNode> findByTemplateVersionId(Long templateVersionId);
}
```

```java
// WorkflowNodeConnectionRepository.java
package com.example.contract.repository;

import com.example.contract.entity.WorkflowNodeConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WorkflowNodeConnectionRepository extends JpaRepository<WorkflowNodeConnection, Long> {
    List<WorkflowNodeConnection> findByTemplateVersionId(Long templateVersionId);
    List<WorkflowNodeConnection> findBySourceNodeId(String sourceNodeId);
}
```

```java
// WorkflowInstanceRepository.java
package com.example.contract.repository;

import com.example.contract.entity.WorkflowInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, Long> {
    Optional<WorkflowInstance> findByInstanceId(String instanceId);
    List<WorkflowInstance> findByContractId(Long contractId);
    List<WorkflowInstance> findByStatus(String status);
}
```

```java
// WorkflowInstanceNodeRepository.java
package com.example.contract.repository;

import com.example.contract.entity.WorkflowInstanceNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowInstanceNodeRepository extends JpaRepository<WorkflowInstanceNode, Long> {
    List<WorkflowInstanceNode> findByInstanceId(Long instanceId);
    Optional<WorkflowInstanceNode> findByInstanceIdAndNodeIdAndStatus(Long instanceId, String nodeId, String status);
}
```

```java
// WorkflowExecutionLogRepository.java
package com.example.contract.repository;

import com.example.contract.entity.WorkflowExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WorkflowExecutionLogRepository extends JpaRepository<WorkflowExecutionLog, Long> {
    List<WorkflowExecutionLog> findByInstanceIdOrderByEventTimeAsc(Long instanceId);
    List<WorkflowExecutionLog> findByNodeId(String nodeId);
}
```

- [ ] **Step 4: 提交**
```bash
git add src/main/java/com/example/contract/repository/Workflow*.java
git commit -m "feat: add workflow repositories"
```

---

### Task 3: DTO创建

**Files:**
- Create: `src/main/java/com/example/contract/dto/request/WorkflowTemplateRequest.java`
- Create: `src/main/java/com/example/contract/dto/request/WorkflowNodeRequest.java`
- Create: `src/main/java/com/example/contract/dto/request/WorkflowInstanceRequest.java`
- Create: `src/main/java/com/example/contract/dto/response/WorkflowTemplateResponse.java`
- Create: `src/main/java/com/example/contract/dto/response/WorkflowDesignerResponse.java`
- Create: `src/main/java/com/example/contract/dto/response/WorkflowStatusResponse.java`

- [ ] **Step 1: 创建请求DTO**

```java
// WorkflowTemplateRequest.java
package com.example.contract.dto.request;

import lombok.Data;
@Data
public class WorkflowTemplateRequest {
    private String name;
    private String description;
    private String category;
    private String bpmnXml;
    private String nodeConfigJson;
}
```

```java
// WorkflowNodeRequest.java
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
```

```java
// WorkflowInstanceRequest.java
package com.example.contract.dto.request;

import lombok.Data;
@Data
public class WorkflowInstanceRequest {
    private Long contractId;
    private Long templateId;
}
```

- [ ] **Step 2: 创建响应DTO**

```java
// WorkflowTemplateResponse.java
package com.example.contract.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WorkflowTemplateResponse {
    private Long id;
    private String name;
    private String description;
    private String category;
    private Integer currentVersion;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

```java
// WorkflowDesignerResponse.java
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
```

```java
// WorkflowStatusResponse.java
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
```

- [ ] **Step 3: 提交**
```bash
git add src/main/java/com/example/contract/dto/request/Workflow*.java
git add src/main/java/com/example/contract/dto/response/Workflow*.java
git commit -m "feat: add workflow DTOs"
```

---

### Task 4: Service层实现

**Files:**
- Create: `src/main/java/com/example/contract/service/WorkflowTemplateService.java`
- Create: `src/main/java/com/example/contract/service/WorkflowDesignerService.java`
- Create: `src/main/java/com/example/contract/service/WorkflowExecutionEngine.java`

**Interfaces:**
- Consumes: Repository接口
- Produces: 业务流程服务

- [ ] **Step 1: WorkflowTemplateService.java**
```java
package com.example.contract.service;

import com.example.contract.entity.*;
import com.example.contract.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkflowTemplateService {
    private final WorkflowTemplateRepository templateRepository;
    private final WorkflowTemplateVersionRepository versionRepository;
    private final WorkflowNodeRepository nodeRepository;
    private final WorkflowNodeConnectionRepository connectionRepository;
    
    public List<WorkflowTemplate> getAllTemplates() {
        return templateRepository.findByIsActiveTrue();
    }
    
    @Transactional
    public WorkflowTemplate createTemplate(String name, String category, String description) {
        WorkflowTemplate template = new WorkflowTemplate();
        template.setName(name);
        template.setCategory(category);
        template.setDescription(description);
        template.setIsActive(true);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        return templateRepository.save(template);
    }
    
    @Transactional
    public WorkflowTemplateVersion saveTemplateVersion(Long templateId, String bpmnXml, String nodeConfigJson) {
        // 停用旧版本
        versionRepository.findByTemplateIdAndIsActiveTrue(templateId)
            .ifPresent(v -> {
                v.setIsActive(false);
                versionRepository.save(v);
            });
        
        // 创建新版本
        WorkflowTemplateVersion latest = versionRepository
            .findTopByTemplateIdOrderByVersionDesc(templateId)
            .orElse(null);
        int newVersion = latest == null ? 1 : latest.getVersion() + 1;
        
        WorkflowTemplateVersion version = new WorkflowTemplateVersion();
        version.setTemplateId(templateId);
        version.setVersion(newVersion);
        version.setBpmnXml(bpmnXml);
        version.setNodeConfigJson(nodeConfigJson);
        version.setIsActive(true);
        version.setCreatedAt(LocalDateTime.now());
        return versionRepository.save(version);
    }
}
```

- [ ] **Step 2: WorkflowDesignerService.java**
```java
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
```

- [ ] **Step 3: WorkflowExecutionEngine.java**
```java
package com.example.contract.service;

import com.example.contract.entity.*;
import com.example.contract.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

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
    
    private final Map<String, Evaluator> conditionEvaluators = new HashMap<>();
    
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
```

- [ ] **Step 4: 提交**
```bash
git add src/main/java/com/example/contract/service/Workflow*.java
git commit -m "feat: add workflow services"
```

---

### Task 5: Controller层实现

**Files:**
- Create: `src/main/java/com/example/contract/controller/WorkflowController.java`

**Interfaces:**
- Consumes: Service接口
- Produces: REST API端点

- [ ] **Step 1: 创建WorkflowController.java**
```java
package com.example.contract.controller;

import com.example.contract.entity.WorkflowInstance;
import com.example.contract.entity.WorkflowTemplate;
import com.example.contract.service.WorkflowTemplateService;
import com.example.contract.service.WorkflowDesignerService;
import com.example.contract.service.WorkflowExecutionEngine;
import com.example.contract.dto.request.WorkflowTemplateRequest;
import com.example.contract.dto.response.WorkflowDesignerResponse;
import com.example.contract.dto.response.WorkflowStatusResponse;
import com.example.contract.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/workflow")
@RequiredArgsConstructor
public class WorkflowController {
    private final WorkflowTemplateService templateService;
    private final WorkflowDesignerService designerService;
    private final WorkflowExecutionEngine executionEngine;
    
    // 模板管理
    @GetMapping("/templates")
    public ApiResponse<List<WorkflowTemplate>> getAllTemplates() {
        return ApiResponse.success(templateService.getAllTemplates());
    }
    
    @PostMapping("/templates")
    public ApiResponse<WorkflowTemplate> createTemplate(@RequestBody WorkflowTemplateRequest request) {
        WorkflowTemplate template = templateService.createTemplate(
            request.getName(), request.getCategory(), request.getDescription());
        return ApiResponse.success(template);
    }
    
    @PostMapping("/templates/{templateId}/versions")
    public ApiResponse<?> saveVersion(@PathVariable Long templateId, @RequestBody WorkflowTemplateRequest request) {
        var version = templateService.saveTemplateVersion(templateId, request.getBpmnXml(), request.getNodeConfigJson());
        return ApiResponse.success(version);
    }
    
    // 流程设计器
    @GetMapping("/designer/{templateVersionId}")
    public ApiResponse<WorkflowDesignerResponse> getDesignerData(@PathVariable Long templateVersionId) {
        return ApiResponse.success(designerService.getDesignerData(templateVersionId));
    }
    
    @PutMapping("/designer/{templateVersionId}")
    public ApiResponse<?> saveDesignerData(@PathVariable Long templateVersionId, @RequestBody WorkflowDesignerResponse data) {
        designerService.saveDesignerData(templateVersionId, data);
        return ApiResponse.success(null);
    }
    
    // 流程执行
    @PostMapping("/instances/start")
    public ApiResponse<WorkflowInstance> startWorkflow(@RequestParam Long contractId, @RequestParam Long templateVersionId) {
        WorkflowInstance instance = executionEngine.startWorkflow(contractId, templateVersionId);
        return ApiResponse.success(instance);
    }
    
    @PostMapping("/instances/{instanceId}/complete")
    public ApiResponse<?> completeNode(@PathVariable Long instanceId, @RequestParam String nodeId, 
            @RequestParam String result, @RequestParam(required = false) Long signerId) {
        executionEngine.completeNode(instanceId, nodeId, result, signerId);
        return ApiResponse.success(null);
    }
    
    @GetMapping("/instances/{instanceId}/status")
    public ApiResponse<WorkflowStatusResponse> getStatus(@PathVariable Long instanceId) {
        return ApiResponse.success(executionEngine.getWorkflowStatus(instanceId));
    }
    
    @GetMapping("/instances/{instanceId}/logs")
    public ApiResponse<?> getExecutionLogs(@PathVariable Long instanceId) {
        return ApiResponse.success(null); // 待实现日志查询
    }
}
```

- [ ] **Step 2: 提交**
```bash
git add src/main/java/com/example/contract/controller/WorkflowController.java
git commit -m "feat: add workflow controller"
```

---

### Task 6: 前端流程设计器

**Files:**
- Create: `src/views/workflow/designer.vue`
- Create: `src/views/workflow/template-list.vue`
- Create: `src/views/workflow/instance-list.vue`

**Interfaces:**
- Consumes: WorkflowController API
- Produces: Vue.js组件

- [ ] **Step 1: 创建流程设计器组件designer.vue**
```vue
<template>
  <div class="workflow-designer">
    <el-container>
      <el-aside width="200px">
        <el-tree :data="nodeTypes" @node-click="addNode">
          <span slot-scope="{ data }">
            <i :class="data.icon"></i> {{ data.label }}
          </span>
        </el-tree>
      </el-aside>
      <el-main>
        <div ref="canvas" class="bpmn-canvas"></div>
      </el-main>
      <el-aside width="300px">
        <el-form v-if="selectedNode" :model="selectedNode" label-width="120px">
          <el-form-item label="节点名称">
            <el-input v-model="selectedNode.nodeName"></el-input>
          </el-form-item>
          <el-form-item label="节点类型">
            <el-select v-model="selectedNode.nodeType">
              <el-option label="签署节点" value="SIGN_SIGNER"></el-option>
              <el-option label="条件分支" value="CONDITION"></el-option>
              <el-option label="并行签署组" value="PARALLEL_GROUP"></el-option>
              <el-option label="超时处理" value="TIMEOUT"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="签署方" v-if="selectedNode.nodeType === 'SIGN_SIGNER'">
            <el-select v-model="selectedNode.config.signerId" placeholder="选择签署方">
              <el-option v-for="s in signers" :key="s.id" :label="s.name" :value="s.id"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="签署方式" v-if="selectedNode.nodeType === 'SIGN_SIGNER'">
            <el-select v-model="selectedNode.config.signMethod">
              <el-option label="顺序签署" value="SEQUENTIAL"></el-option>
              <el-option label="并行签署" value="PARALLEL"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="顺序权重" v-if="selectedNode.nodeType === 'SIGN_SIGNER'">
            <el-input-number v-model="selectedNode.config.orderWeight" :min="1"></el-input-number>
          </el-form-item>
          <el-form-item label="超时时间(小时)">
            <el-input-number v-model="selectedNode.timeoutHours" :min="0"></el-input-number>
          </el-form-item>
          <el-form-item label="超时动作">
            <el-select v-model="selectedNode.timeoutAction">
              <el-option label="自动跳过" value="AUTO_SKIP"></el-option>
              <el-option label="发送提醒" value="REMIND"></el-option>
              <el-option label="升级处理" value="ESCALATE"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="条件表达式" v-if="selectedNode.nodeType === 'CONDITION'">
            <el-input v-model="selectedNode.config.conditionExpression" placeholder="如: signerCount > 3"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveNode">保存节点</el-button>
            <el-button type="danger" @click="deleteNode">删除节点</el-button>
          </el-form-item>
        </el-form>
      </el-aside>
    </el-container>
    <el-footer>
      <el-button @click="loadTemplate">加载模板</el-button>
      <el-button @click="saveTemplate">保存模板</el-button>
      <el-button type="success" @click="validateAndDeploy">验证并发布</el-button>
    </el-footer>
  </div>
</template>

<script>
import BpmnViewer from 'bpmn-js';
import CustomModeler from './CustomModeler';

export default {
  name: 'WorkflowDesigner',
  data() {
    return {
      viewer: null,
      modeler: null,
      selectedNode: null,
      signers: [],
      nodeTypes: [
        { label: '签署节点', icon: 'el-icon-user', type: 'SIGN_SIGNER' },
        { label: '条件分支', icon: 'el-icon-share', type: 'CONDITION' },
        { label: '并行签署组', icon: 'el-icon-more', type: 'PARALLEL_GROUP' },
        { label: '超时处理', icon: 'el-icon-time', type: 'TIMEOUT' },
        { label: '开始节点', icon: 'el-icon-video-play', type: 'START' },
        { label: '结束节点', icon: 'el-icon-video-pause', type: 'END' }
      ]
    };
  },
  mounted() {
    this.initBpmn();
    this.loadSigners();
  },
  methods: {
    initBpmn() {
      this.viewer = new BpmnViewer({ container: this.$refs.canvas });
      this.modeler = new CustomModeler({ container: this.$refs.canvas });
    },
    loadSigners() {
      // 从API加载签署方列表
      this.$http.get('/api/signers').then(res => {
        this.signers = res.data;
      });
    },
    addNode(data) {
      // 在画布上添加新节点
      this.modeler.addNode(data.type);
    },
    saveNode() {
      if (!this.selectedNode) return;
      this.$message.success('节点保存成功');
    },
    deleteNode() {
      this.$message.info('节点删除');
    },
    loadTemplate() {
      // 加载模板数据
    },
    saveTemplate() {
      // 保存模板
    },
    validateAndDeploy() {
      // 验证并发布
    }
  }
};
</script>

<style scoped>
.workflow-designer {
  height: 100%;
}
.bpmn-canvas {
  height: 500px;
  border: 1px solid #ddd;
}
</style>
```

- [ ] **Step 2: 创建模板列表组件template-list.vue**
```vue
<template>
  <div class="template-list">
    <el-table :data="templates" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="name" label="模板名称"></el-table-column>
      <el-table-column prop="category" label="类别"></el-table-column>
      <el-table-column prop="currentVersion" label="版本" width="80"></el-table-column>
      <el-table-column prop="updatedAt" label="更新时间"></el-table-column>
      <el-table-column label="操作" width="200">
        <template slot-scope="scope">
          <el-button size="mini" @click="editTemplate(scope.row)">编辑</el-button>
          <el-button size="mini" type="primary" @click="openDesigner(scope.row)">设计流程</el-button>
          <el-button size="mini" type="danger" @click="disableTemplate(scope.row)">禁用</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-button type="primary" @click="createTemplate">新建模板</el-button>
  </div>
</template>

<script>
export default {
  name: 'TemplateList',
  data() {
    return {
      templates: []
    };
  },
  mounted() {
    this.loadTemplates();
  },
  methods: {
    loadTemplates() {
      this.$http.get('/api/workflow/templates').then(res => {
        this.templates = res.data;
      });
    },
    editTemplate(row) {
      this.$message.info('编辑模板');
    },
    openDesigner(row) {
      this.$router.push(`/workflow/designer/${row.currentVersionId}`);
    },
    disableTemplate(row) {
      this.$message.info('禁用模板');
    },
    createTemplate() {
      this.$message.info('创建模板');
    }
  }
};
</script>
```

- [ ] **Step 3: 提交**
```bash
git add src/views/workflow/designer.vue src/views/workflow/template-list.vue
git commit -m "feat: add workflow designer frontend"
```

---

### Task 7: 合同创建集成

**Files:**
- Modify: `src/main/java/com/example/contract/entity/Contract.java`
- Modify: `src/main/java/com/example/contract/service/ContractService.java`

**Interfaces:**
- Consumes: WorkflowExecutionEngine
- Produces: 合同创建时自动初始化流程

- [ ] **Step 1: 修改Contract.java添加流程实例关联**
```java
// 在Contract.java中添加
private Long workflowInstanceId;  // 关联流程实例
private Long workflowTemplateId;  // 创建时使用的模板ID
private Integer workflowTemplateVersion;  // 模板版本号
```

- [ ] **Step 2: 修改ContractService.java**
```java
// 在createContract方法中添加
@Autowired
private WorkflowExecutionEngine workflowExecutionEngine;

public Contract createContract(ContractCreateRequest request) {
    Contract contract = new Contract();
    // ... 设置合同属性
    
    contract = contractRepository.save(contract);
    
    // 如果选择了模板，自动启动流程
    if (request.getTemplateId() != null) {
        Long templateVersionId = getActiveTemplateVersionId(request.getTemplateId());
        WorkflowInstance instance = workflowExecutionEngine.startWorkflow(
            contract.getId(), templateVersionId);
        contract.setWorkflowInstanceId(instance.getId());
        contract.setWorkflowTemplateId(request.getTemplateId());
        contract.setWorkflowTemplateVersion(instance.getTemplateVersionId());
        contractRepository.save(contract);
    }
    
    return contract;
}
```

- [ ] **Step 3: 提交**
```bash
git add src/main/java/com/example/contract/entity/Contract.java
git add src/main/java/com/example/contract/service/ContractService.java
git commit -m "feat: integrate workflow with contract creation"
```

---

## 实施检查清单

- [ ] 实体类创建完成
- [ ] Repository接口创建完成
- [ ] DTO类创建完成
- [ ] WorkflowTemplateService实现
- [ ] WorkflowDesignerService实现
- [ ] WorkflowExecutionEngine实现
- [ ] WorkflowController实现
- [ ] 前端流程设计器组件
- [ ] 合同创建集成流程引擎
- [ ] 数据库迁移脚本

---

## 依赖项

- bpmn-js: ^8.0.0 (流程设计器前端库)
- com.fasterxml.jackson.core:jackson-databind (JSON处理)
- 现有项目已包含JPA、MySQL、Redis配置