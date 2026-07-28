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