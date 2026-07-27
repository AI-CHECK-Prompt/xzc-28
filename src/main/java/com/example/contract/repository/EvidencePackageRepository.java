package com.example.contract.repository;

import com.example.contract.entity.EvidencePackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 证据包数据访问层
 */
@Repository
public interface EvidencePackageRepository extends JpaRepository<EvidencePackage, Long> {

    /**
     * 根据证据包编号查询证据包
     */
    Optional<EvidencePackage> findByPackageNo(String packageNo);

    /**
     * 根据合同ID查询证据包列表
     */
    List<EvidencePackage> findByContractId(Long contractId);

    /**
     * 根据案件编号查询证据包列表
     */
    List<EvidencePackage> findByCaseNo(String caseNo);

    /**
     * 根据证据包类型查询证据包列表
     */
    List<EvidencePackage> findByType(EvidencePackage.PackageType type);

    /**
     * 根据证据包状态查询证据包列表
     */
    List<EvidencePackage> findByStatus(EvidencePackage.PackageStatus status);

    /**
     * 根据链上验证状态查询证据包列表
     */
    List<EvidencePackage> findByChainVerifyStatus(EvidencePackage.ChainVerifyStatus status);

    /**
     * 根据可信度评分范围查询证据包
     */
    List<EvidencePackage> findByCredibilityScoreBetween(Integer minScore, Integer maxScore);

    /**
     * 检查证据包编号是否已存在
     */
    boolean existsByPackageNo(String packageNo);

}