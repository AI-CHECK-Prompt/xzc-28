package com.example.contract.repository;

import com.example.contract.entity.SignRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 签署记录数据访问层
 */
@Repository
public interface SignRecordRepository extends JpaRepository<SignRecord, Long> {

    /**
     * 根据合同ID查询签署记录列表
     */
    List<SignRecord> findByContractIdOrderBySignOrder(Long contractId);

    /**
     * 根据签约方ID查询签署记录列表
     */
    List<SignRecord> findBySignerId(Long signerId);

    /**
     * 根据合同ID和签约方ID查询签署记录
     */
    Optional<SignRecord> findByContractIdAndSignerId(Long contractId, Long signerId);

    /**
     * 根据签署状态查询签署记录列表
     */
    List<SignRecord> findByStatus(SignRecord.SignStatus status);

    /**
     * 根据签署类型查询签署记录列表
     */
    List<SignRecord> findBySignType(SignRecord.SignType signType);

    /**
     * 根据链上交易哈希查询签署记录
     */
    Optional<SignRecord> findByFabricTxHash(String txHash);

    /**
     * 查询合同的已完成签署记录数
     */
    long countByContractIdAndStatus(Long contractId, SignRecord.SignStatus status);

    /**
     * 查询合同的最大签署顺序
     */
    Integer findMaxSignOrderByContractId(Long contractId);

}