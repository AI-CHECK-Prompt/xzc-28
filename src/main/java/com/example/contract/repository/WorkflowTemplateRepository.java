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