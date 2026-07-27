package com.example.contract.repository;

import com.example.contract.entity.SmartContractEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 智能合约事件数据访问层
 */
@Repository
public interface SmartContractEventRepository extends JpaRepository<SmartContractEvent, Long> {

    /**
     * 根据合同ID查询智能合约事件列表
     */
    List<SmartContractEvent> findByContractIdOrderByCreatedAtDesc(Long contractId);

    /**
     * 根据事件类型查询事件列表
     */
    List<SmartContractEvent> findByEventType(SmartContractEvent.EventType eventType);

    /**
     * 根据事件级别查询事件列表
     */
    List<SmartContractEvent> findByLevel(SmartContractEvent.EventLevel level);

    /**
     * 根据处理状态查询事件列表
     */
    List<SmartContractEvent> findByProcessStatus(SmartContractEvent.ProcessStatus processStatus);

    /**
     * 根据链上交易哈希查询事件
     */
    List<SmartContractEvent> findByTxHash(String txHash);

    /**
     * 查询未处理的事件列表
     */
    List<SmartContractEvent> findByProcessStatusOrderByCreatedAtDesc(SmartContractEvent.ProcessStatus status);

    /**
     * 查询指定级别以上的未处理事件
     */
    List<SmartContractEvent> findByLevelInAndProcessStatusOrderByCreatedAtDesc(
            List<SmartContractEvent.EventLevel> levels, 
            SmartContractEvent.ProcessStatus status);

}