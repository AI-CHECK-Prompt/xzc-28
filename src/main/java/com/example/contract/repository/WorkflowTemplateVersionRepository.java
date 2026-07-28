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