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