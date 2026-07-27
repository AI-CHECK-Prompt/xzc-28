package com.example.contract.service;

import com.example.contract.dto.request.ContractCreateRequest;
import com.example.contract.dto.response.ContractResponse;
import com.example.contract.dto.response.SignerResponse;
import com.example.contract.dto.response.SignRecordResponse;
import com.example.contract.entity.Contract;
import com.example.contract.entity.Signer;
import com.example.contract.entity.SignRecord;
import com.example.contract.exception.BusinessException;
import com.example.contract.repository.ContractRepository;
import com.example.contract.repository.SignRecordRepository;
import com.example.contract.util.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 合同服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final SignRecordRepository signRecordRepository;
    private final SignerService signerService;

    /**
     * 创建合同
     */
    @Transactional
    public ContractResponse createContract(ContractCreateRequest request) {
        // 验证签约方存在
        for (Long signerId : request.getSignerIds()) {
            signerService.getSignerEntity(signerId);
        }

        // 解码合同内容并计算哈希
        byte[] contentBytes = Base64.getDecoder().decode(request.getContentBase64());
        String contentHash = HashUtil.sha256(contentBytes);

        // 创建合同
        Contract contract = Contract.builder()
                .contractNo(HashUtil.generateContractNo())
                .name(request.getName())
                .type(Contract.ContractType.valueOf(request.getType()))
                .status(Contract.ContractStatus.PENDING)
                .contentHash(contentHash)
                .signDeadline(request.getSignDeadline())
                .effectiveTime(request.getEffectiveTime())
                .expireTime(request.getExpireTime())
                .creatorId(request.getCreatorId())
                .evidenceStatus(Contract.EvidenceStatus.PENDING)
                .build();

        Contract saved = contractRepository.save(contract);
        log.info("创建合同成功: contractNo={}, name={}", saved.getContractNo(), saved.getName());

        // 创建待签署记录
        int order = 1;
        for (Long signerId : request.getSignerIds()) {
            SignRecord signRecord = SignRecord.builder()
                    .contractId(saved.getId())
                    .signerId(signerId)
                    .signOrder(order++)
                    .signType(SignRecord.SignType.INITIAL)
                    .status(SignRecord.SignStatus.PENDING)
                    .actionHash("")
                    .build();
            signRecordRepository.save(signRecord);
        }

        return toResponse(saved);
    }

    /**
     * 根据合同编号查询合同
     */
    public ContractResponse getContractByNo(String contractNo) {
        Contract contract = contractRepository.findByContractNo(contractNo)
                .orElseThrow(() -> BusinessException.notFound("合同不存在"));
        return toResponse(contract);
    }

    /**
     * 根据ID查询合同
     */
    public ContractResponse getContractById(Long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("合同不存在"));
        return toResponse(contract);
    }

    /**
     * 查询所有合同
     */
    public List<ContractResponse> getAllContracts() {
        return contractRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 根据状态查询合同
     */
    public List<ContractResponse> getContractsByStatus(String status) {
        Contract.ContractStatus contractStatus = Contract.ContractStatus.valueOf(status);
        return contractRepository.findByStatus(contractStatus).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 更新合同
     */
    @Transactional
    public ContractResponse updateContract(Long id, ContractCreateRequest request) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("合同不存在"));

        contract.setName(request.getName());
        contract.setType(Contract.ContractType.valueOf(request.getType()));
        contract.setSignDeadline(request.getSignDeadline());
        contract.setEffectiveTime(request.getEffectiveTime());
        contract.setExpireTime(request.getExpireTime());

        // 如果内容变更，重新计算哈希
        if (request.getContentBase64() != null && !request.getContentBase64().isEmpty()) {
            byte[] contentBytes = Base64.getDecoder().decode(request.getContentBase64());
            contract.setContentHash(HashUtil.sha256(contentBytes));
        }

        Contract saved = contractRepository.save(contract);
        log.info("更新合同成功: id={}", saved.getId());
        return toResponse(saved);
    }

    /**
     * 删除合同
     */
    @Transactional
    public void deleteContract(Long id) {
        if (!contractRepository.existsById(id)) {
            throw BusinessException.notFound("合同不存在");
        }
        contractRepository.deleteById(id);
        log.info("删除合同成功: id={}", id);
    }

    /**
     * 获取合同实体（内部使用）
     */
    public Contract getContractEntity(Long id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("合同不存在"));
    }

    /**
     * 获取合同实体（通过合同编号）
     */
    public Contract getContractEntityByNo(String contractNo) {
        return contractRepository.findByContractNo(contractNo)
                .orElseThrow(() -> BusinessException.notFound("合同不存在"));
    }

    /**
     * 更新合同状态
     */
    @Transactional
    public void updateContractStatus(Long contractId, Contract.ContractStatus status) {
        Contract contract = getContractEntity(contractId);
        contract.setStatus(status);
        contractRepository.save(contract);
    }

    /**
     * 转换为响应DTO
     */
    private ContractResponse toResponse(Contract contract) {
        // 获取签署记录
        List<SignRecord> signRecords = signRecordRepository.findByContractIdOrderBySignOrder(contract.getId());

        // 获取签约方信息
        List<SignerResponse> signers = signRecords.stream()
                .map(record -> signerService.getSignerById(record.getSignerId()))
                .distinct()
                .collect(Collectors.toList());

        // 转换签署记录
        List<SignRecordResponse> signRecordResponses = signRecords.stream()
                .map(record -> SignRecordResponse.builder()
                        .id(record.getId())
                        .contractNo(contract.getContractNo())
                        .signer(signerService.getSignerById(record.getSignerId()))
                        .signOrder(record.getSignOrder())
                        .signType(record.getSignType().name())
                        .status(record.getStatus().name())
                        .signTime(record.getSignTime())
                        .timeSource(record.getTimeSource() != null ? record.getTimeSource().name() : null)
                        .actionHash(record.getActionHash())
                        .deviceType(record.getDeviceType() != null ? record.getDeviceType().name() : null)
                        .ipAddress(record.getIpAddress())
                        .signatureAlgorithm(record.getSignatureAlgorithm())
                        .fabricTxHash(record.getFabricTxHash())
                        .createdAt(record.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return ContractResponse.builder()
                .id(contract.getId())
                .contractNo(contract.getContractNo())
                .name(contract.getName())
                .type(contract.getType().name())
                .status(contract.getStatus().name())
                .contentHash(contract.getContentHash())
                .signDeadline(contract.getSignDeadline())
                .effectiveTime(contract.getEffectiveTime())
                .expireTime(contract.getExpireTime())
                .fabricTxHash(contract.getFabricTxHash())
                .ethTxHash(contract.getEthTxHash())
                .evidenceStatus(contract.getEvidenceStatus().name())
                .credibilityScore(contract.getCredibilityScore())
                .signers(signers)
                .signRecords(signRecordResponses)
                .createdAt(contract.getCreatedAt())
                .updatedAt(contract.getUpdatedAt())
                .lastSignTime(contract.getLastSignTime())
                .build();
    }

}