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