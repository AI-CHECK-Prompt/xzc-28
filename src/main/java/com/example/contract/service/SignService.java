package com.example.contract.service;

import com.example.contract.dto.request.SignRequest;
import com.example.contract.dto.response.SignRecordResponse;
import com.example.contract.entity.Contract;
import com.example.contract.entity.Signer;
import com.example.contract.entity.SignRecord;
import com.example.contract.exception.BusinessException;
import com.example.contract.repository.SignRecordRepository;
import com.example.contract.util.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 签署服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignService {

    private final SignRecordRepository signRecordRepository;
    private final ContractService contractService;
    private final SignerService signerService;
    private final BlockchainService blockchainService;
    private final EvidencePackageService evidencePackageService;

    /**
     * 执行签署
     */
    @Transactional
    public SignRecordResponse sign(SignRequest request) {
        // 获取合同
        Contract contract = contractService.getContractEntityByNo(request.getContractNo());
        
        // 检查合同状态
        if (contract.getStatus() == Contract.ContractStatus.SIGNED) {
            throw BusinessException.badRequest("合同已完成签署");
        }
        if (contract.getStatus() == Contract.ContractStatus.REVOKED) {
            throw BusinessException.badRequest("合同已撤销");
        }

        // 获取签约方
        Signer signer = signerService.getSignerEntity(request.getSignerId());

        // 检查是否有权限签署
        SignRecord existingRecord = signRecordRepository
                .findByContractIdAndSignerId(contract.getId(), signer.getId())
                .orElseThrow(() -> BusinessException.badRequest("该签约方不是合同签署方"));

        // 检查是否已签署
        if (existingRecord.getStatus() == SignRecord.SignStatus.COMPLETED) {
            throw BusinessException.badRequest("该签约方已完成签署");
        }

        // 获取当前时间（权威时间源）
        LocalDateTime signTime = LocalDateTime.now();

        // 计算签署行为哈希
        String actionHash = HashUtil.combineHash(
                contract.getContractNo(),
                signer.getIdentityStamp(),
                String.valueOf(signTime),
                request.getSignatureBase64(),
                request.getIpAddress()
        );

        // 更新签署记录
        existingRecord.setStatus(SignRecord.SignStatus.COMPLETED);
        existingRecord.setSignTime(signTime);
        existingRecord.setTimeSource(SignRecord.TimeSource.BLOCKCHAIN);
        existingRecord.setActionHash(actionHash);
        existingRecord.setSignature(request.getSignatureBase64());
        existingRecord.setSignatureAlgorithm(request.getSignatureAlgorithm() != null ? request.getSignatureAlgorithm() : "SM2");
        
        if (request.getDeviceType() != null) {
            existingRecord.setDeviceType(SignRecord.DeviceType.valueOf(request.getDeviceType()));
        }
        existingRecord.setDeviceModel(request.getDeviceModel());
        existingRecord.setOs(request.getOs());
        existingRecord.setIpAddress(request.getIpAddress());
        existingRecord.setNetworkType(request.getNetworkType());
        existingRecord.setLocation(request.getLocation());
        existingRecord.setUserAgent(request.getUserAgent());
        existingRecord.setCertificateChain(request.getCertificateChainBase64());
        
        // 保存签署时的认证级别快照（用于评分计算，避免时间窗口耦合）
        existingRecord.setSignerAuthLevel(signer.getAuthLevel());
        existingRecord.setSignerAuthStatus(signer.getAuthStatus());

        // 上链存证
        String txHash = blockchainService.storeEvidence(actionHash, contract.getId());
        existingRecord.setFabricTxHash(txHash);

        SignRecord saved = signRecordRepository.save(existingRecord);
        log.info("签署成功: contractNo={}, signerId={}, txHash={}", 
                contract.getContractNo(), signer.getId(), txHash);

        // 更新合同状态
        updateContractSignStatus(contract.getId());

        return toResponse(saved, contract.getContractNo());
    }

    /**
     * 查询合同的签署记录
     */
    public List<SignRecordResponse> getSignRecords(Long contractId) {
        Contract contract = contractService.getContractEntity(contractId);
        return signRecordRepository.findByContractIdOrderBySignOrder(contractId).stream()
                .map(record -> toResponse(record, contract.getContractNo()))
                .collect(Collectors.toList());
    }

    /**
     * 根据ID查询签署记录
     */
    public SignRecordResponse getSignRecordById(Long id) {
        SignRecord record = signRecordRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("签署记录不存在"));
        Contract contract = contractService.getContractEntity(record.getContractId());
        return toResponse(record, contract.getContractNo());
    }

    /**
     * 更新合同签署状态
     */
    private void updateContractSignStatus(Long contractId) {
        Contract contract = contractService.getContractEntity(contractId);
        
        long completedCount = signRecordRepository.countByContractIdAndStatus(contractId, SignRecord.SignStatus.COMPLETED);
        long totalCount = signRecordRepository.countByContractId(contractId);

        if (completedCount == totalCount) {
            contractService.updateContractStatus(contractId, Contract.ContractStatus.SIGNED);
            contract.setLastSignTime(LocalDateTime.now());
            // 触发自动证据包组装
            evidencePackageService.generateEvidencePackage(contractId);
        } else if (completedCount > 0) {
            contractService.updateContractStatus(contractId, Contract.ContractStatus.PARTIAL);
        }
    }

    /**
     * 转换为响应DTO
     */
    private SignRecordResponse toResponse(SignRecord record, String contractNo) {
        return SignRecordResponse.builder()
                .id(record.getId())
                .contractNo(contractNo)
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
                .build();
    }

}