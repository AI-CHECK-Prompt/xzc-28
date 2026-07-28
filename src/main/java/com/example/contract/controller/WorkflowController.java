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