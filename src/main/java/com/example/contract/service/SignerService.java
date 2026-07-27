package com.example.contract.service;

import com.example.contract.dto.request.SignerCreateRequest;
import com.example.contract.dto.response.SignerResponse;
import com.example.contract.entity.Signer;
import com.example.contract.exception.BusinessException;
import com.example.contract.repository.SignerRepository;
import com.example.contract.util.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 签约方服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignerService {

    private final SignerRepository signerRepository;

    /**
     * 创建签约方
     */
    @Transactional
    public SignerResponse createSigner(SignerCreateRequest request) {
        // 检查证件号是否已存在
        if (signerRepository.existsByIdCard(request.getIdCard())) {
            throw BusinessException.badRequest("证件号码已被注册");
        }

        Signer signer = Signer.builder()
                .name(request.getName())
                .idCard(request.getIdCard())
                .type(Signer.SignerType.valueOf(request.getType()))
                .phone(request.getPhone())
                .email(request.getEmail())
                .authStatus(Signer.AuthStatus.UNVERIFIED)
                .identityStamp(HashUtil.generateIdentityStamp(request.getIdCard()))
                .build();

        Signer saved = signerRepository.save(signer);
        log.info("创建签约方成功: id={}, name={}", saved.getId(), saved.getName());
        return toResponse(saved);
    }

    /**
     * 根据ID查询签约方
     */
    public SignerResponse getSignerById(Long id) {
        Signer signer = signerRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("签约方不存在"));
        return toResponse(signer);
    }

    /**
     * 根据身份证号查询签约方
     */
    public SignerResponse getSignerByIdCard(String idCard) {
        Signer signer = signerRepository.findByIdCard(idCard)
                .orElseThrow(() -> BusinessException.notFound("签约方不存在"));
        return toResponse(signer);
    }

    /**
     * 查询所有签约方
     */
    public List<SignerResponse> getAllSigners() {
        return signerRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 根据类型查询签约方
     */
    public List<SignerResponse> getSignersByType(String type) {
        Signer.SignerType signerType = Signer.SignerType.valueOf(type);
        return signerRepository.findByType(signerType).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 更新签约方信息
     */
    @Transactional
    public SignerResponse updateSigner(Long id, SignerCreateRequest request) {
        Signer signer = signerRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("签约方不存在"));

        signer.setName(request.getName());
        signer.setPhone(request.getPhone());
        signer.setEmail(request.getEmail());

        Signer saved = signerRepository.save(signer);
        log.info("更新签约方成功: id={}", saved.getId());
        return toResponse(saved);
    }

    /**
     * 删除签约方
     */
    @Transactional
    public void deleteSigner(Long id) {
        if (!signerRepository.existsById(id)) {
            throw BusinessException.notFound("签约方不存在");
        }
        signerRepository.deleteById(id);
        log.info("删除签约方成功: id={}", id);
    }

    /**
     * 获取签约方实体（内部使用）
     */
    public Signer getSignerEntity(Long id) {
        return signerRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("签约方不存在"));
    }

    /**
     * 转换为响应DTO
     */
    private SignerResponse toResponse(Signer signer) {
        return SignerResponse.builder()
                .id(signer.getId())
                .name(signer.getName())
                .idCardMasked(maskIdCard(signer.getIdCard()))
                .type(signer.getType().name())
                .phone(signer.getPhone())
                .email(signer.getEmail())
                .authStatus(signer.getAuthStatus().name())
                .authLevel(signer.getAuthLevel() != null ? signer.getAuthLevel().name() : null)
                .identityStamp(signer.getIdentityStamp())
                .createdAt(signer.getCreatedAt())
                .build();
    }

    /**
     * 证件号脱敏处理
     */
    private String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return idCard;
        }
        return idCard.substring(0, 4) + "****" + idCard.substring(idCard.length() - 4);
    }

}