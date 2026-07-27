package com.example.contract.repository;

import com.example.contract.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 合同数据访问层
 */
@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    /**
     * 根据合同编号查询合同
     */
    Optional<Contract> findByContractNo(String contractNo);

    /**
     * 根据合同状态查询合同列表
     */
    List<Contract> findByStatus(Contract.ContractStatus status);

    /**
     * 根据合同类型查询合同列表
     */
    List<Contract> findByType(Contract.ContractType type);

    /**
     * 根据存证状态查询合同列表
     */
    List<Contract> findByEvidenceStatus(Contract.EvidenceStatus evidenceStatus);

    /**
     * 根据创建人ID查询合同列表
     */
    List<Contract> findByCreatorId(Long creatorId);

    /**
     * 根据链上交易哈希查询合同（联盟链）
     */
    Optional<Contract> findByFabricTxHash(String txHash);

    /**
     * 根据链上交易哈希查询合同（公有链）
     */
    Optional<Contract> findByEthTxHash(String txHash);

    /**
     * 查询指定时间范围内创建的合同
     */
    List<Contract> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询签署截止时间之前的待签署合同
     */
    List<Contract> findByStatusAndSignDeadlineBefore(Contract.ContractStatus status, LocalDateTime deadline);

    /**
     * 根据合同名称模糊查询
     */
    List<Contract> findByNameContaining(String name);

    /**
     * 检查合同编号是否已存在
     */
    boolean existsByContractNo(String contractNo);

}