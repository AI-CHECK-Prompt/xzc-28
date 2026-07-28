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