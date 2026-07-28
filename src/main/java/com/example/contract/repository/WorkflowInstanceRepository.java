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