package com.example.contract.repository;

import com.example.contract.entity.ContractTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 合约模板数据访问层
 */
@Repository
public interface ContractTemplateRepository extends JpaRepository<ContractTemplate, Long> {

    /**
     * 根据模板编号查询模板
     */
    Optional<ContractTemplate> findByTemplateCode(String templateCode);

    /**
     * 根据模板名称查询模板列表
     */
    List<ContractTemplate> findByNameContaining(String name);

    /**
     * 根据模板类型查询模板列表
     */
    List<ContractTemplate> findByType(ContractTemplate.TemplateType type);

    /**
     * 根据模板状态查询模板列表
     */
    List<ContractTemplate> findByStatus(ContractTemplate.TemplateStatus status);

    /**
     * 查询启用状态的模板列表
     */
    List<ContractTemplate> findByStatusOrderByCreatedAtDesc(ContractTemplate.TemplateStatus status);

    /**
     * 检查模板编号是否已存在
     */
    boolean existsByTemplateCode(String templateCode);

}