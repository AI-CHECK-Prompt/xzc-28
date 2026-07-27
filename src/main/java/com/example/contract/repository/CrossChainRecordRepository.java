package com.example.contract.repository;

import com.example.contract.entity.CrossChainRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 跨链协同记录数据访问层
 */
@Repository
public interface CrossChainRecordRepository extends JpaRepository<CrossChainRecord, Long> {

    /**
     * 根据合同ID查询跨链记录列表
     */
    List<CrossChainRecord> findByContractIdOrderByCreatedAtDesc(Long contractId);

    /**
     * 根据目标链类型查询跨链记录列表
     */
    List<CrossChainRecord> findByTargetChain(CrossChainRecord.TargetChain targetChain);

    /**
     * 根据跨链类型查询跨链记录列表
     */
    List<CrossChainRecord> findByType(CrossChainRecord.CrossChainType type);

    /**
     * 根据跨链状态查询跨链记录列表
     */
    List<CrossChainRecord> findByStatus(CrossChainRecord.CrossChainStatus status);

    /**
     * 根据跨链交易哈希查询跨链记录
     */
    Optional<CrossChainRecord> findByCrossChainTxHash(String txHash);

    /**
     * 根据目标链存证ID查询跨链记录
     */
    Optional<CrossChainRecord> findByTargetEvidenceId(String evidenceId);

    /**
     * 查询指定合同在指定目标链上的跨链记录
     */
    List<CrossChainRecord> findByContractIdAndTargetChain(Long contractId, CrossChainRecord.TargetChain targetChain);

}