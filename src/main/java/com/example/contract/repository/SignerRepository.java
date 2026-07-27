package com.example.contract.repository;

import com.example.contract.entity.Signer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 签约主体数据访问层
 */
@Repository
public interface SignerRepository extends JpaRepository<Signer, Long> {

    /**
     * 根据身份证号/统一社会信用代码查询签约方
     */
    Optional<Signer> findByIdCard(String idCard);

    /**
     * 根据身份戳查询签约方
     */
    Optional<Signer> findByIdentityStamp(String identityStamp);

    /**
     * 根据手机号查询签约方
     */
    List<Signer> findByPhone(String phone);

    /**
     * 根据认证状态查询签约方列表
     */
    List<Signer> findByAuthStatus(Signer.AuthStatus authStatus);

    /**
     * 根据认证级别查询签约方列表
     */
    List<Signer> findByAuthLevel(Signer.AuthLevel authLevel);

    /**
     * 根据签约方类型查询签约方列表
     */
    List<Signer> findByType(Signer.SignerType type);

    /**
     * 检查身份证号是否已存在
     */
    boolean existsByIdCard(String idCard);

}