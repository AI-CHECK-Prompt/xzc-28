package com.example.contract.service;

import com.example.contract.dto.response.EvidencePackageResponse;
import com.example.contract.entity.Contract;
import com.example.contract.entity.EvidencePackage;
import com.example.contract.entity.SignRecord;
import com.example.contract.entity.Signer;
import com.example.contract.exception.BusinessException;
import com.example.contract.repository.ContractRepository;
import com.example.contract.repository.EvidencePackageRepository;
import com.example.contract.repository.SignRecordRepository;
import com.example.contract.util.HashUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.contract.util.HashUtil.formatDateTime;

/**
 * 证据包服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvidencePackageService {

    private final EvidencePackageRepository evidencePackageRepository;
    private final ContractRepository contractRepository;
    private final SignRecordRepository signRecordRepository;
    private final CredibilityService credibilityService;
    private final ObjectMapper objectMapper;
    private final SignerService signerService;

    /**
     * 生成证据包
     */
    @Transactional
    public EvidencePackageResponse generateEvidencePackage(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> BusinessException.notFound("合同不存在"));

        // 生成证据目录
        String evidenceCatalog = generateEvidenceCatalog(contract);

        // 计算证据包哈希
        String packageHash = HashUtil.sha256(evidenceCatalog);

        // 计算可信度评分
        Integer credibilityScore = credibilityService.calculateCredibility(contractId);

        EvidencePackage evidencePackage = EvidencePackage.builder()
                .packageNo(HashUtil.generatePackageNo())
                .contractId(contractId)
                .type(EvidencePackage.PackageType.LEGAL)
                .status(EvidencePackage.PackageStatus.COMPLETED)
                .evidenceCatalog(evidenceCatalog)
                .packageHash(packageHash)
                .credibilityScore(credibilityScore)
                .chainVerifyStatus(EvidencePackage.ChainVerifyStatus.NOT_VERIFIED)
                .expireTime(LocalDateTime.now().plusYears(1))
                .build();

        EvidencePackage saved = evidencePackageRepository.save(evidencePackage);
        log.info("生成证据包成功: packageNo={}, contractId={}", saved.getPackageNo(), contractId);

        return toResponse(saved);
    }

    /**
     * 根据证据包编号查询证据包
     */
    public EvidencePackageResponse getEvidencePackageByNo(String packageNo) {
        EvidencePackage evidencePackage = evidencePackageRepository.findByPackageNo(packageNo)
                .orElseThrow(() -> BusinessException.notFound("证据包不存在"));
        return toResponse(evidencePackage);
    }

    /**
     * 根据合同ID查询证据包列表
     */
    public List<EvidencePackageResponse> getEvidencePackagesByContract(Long contractId) {
        return evidencePackageRepository.findByContractId(contractId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 查询所有证据包
     */
    public List<EvidencePackageResponse> getAllEvidencePackages() {
        return evidencePackageRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 验证证据包
     */
    @Transactional
    public boolean verifyEvidencePackage(Long packageId) {
        EvidencePackage evidencePackage = evidencePackageRepository.findById(packageId)
                .orElseThrow(() -> BusinessException.notFound("证据包不存在"));

        // 验证哈希
        String calculatedHash = HashUtil.sha256(evidencePackage.getEvidenceCatalog());
        boolean isValid = calculatedHash.equals(evidencePackage.getPackageHash());

        if (isValid) {
            evidencePackage.setChainVerifyStatus(EvidencePackage.ChainVerifyStatus.VERIFIED);
            evidencePackage.setVerifiedTime(LocalDateTime.now());
            evidencePackageRepository.save(evidencePackage);
            log.info("证据包验证成功: packageId={}", packageId);
        } else {
            evidencePackage.setChainVerifyStatus(EvidencePackage.ChainVerifyStatus.FAILED);
            evidencePackageRepository.save(evidencePackage);
            log.warn("证据包验证失败: packageId={}", packageId);
        }

        return isValid;
    }

    /**
     * 生成证据目录JSON
     */
    private String generateEvidenceCatalog(Contract contract) {
        List<SignRecord> signRecords = signRecordRepository.findByContractIdOrderBySignOrder(contract.getId());

        Map<String, Object> catalog = new HashMap<>();
        catalog.put("contractNo", contract.getContractNo());
        catalog.put("contractName", contract.getName());
        catalog.put("contentHash", contract.getContentHash());
        catalog.put("fabricTxHash", contract.getFabricTxHash());
        catalog.put("ethTxHash", contract.getEthTxHash());
        catalog.put("evidenceStatus", contract.getEvidenceStatus());
        catalog.put("createdAt", contract.getCreatedAt().toString());

        List<Map<String, Object>> signerInfoList = signRecords.stream().map(record -> {
            Map<String, Object> signerInfo = new HashMap<>();
            signerInfo.put("signOrder", record.getSignOrder());
            signerInfo.put("signerId", record.getSignerId());
            signerInfo.put("signTime", formatDateTime(record.getSignTime()));
            signerInfo.put("timeSource", record.getTimeSource() != null ? record.getTimeSource().name() : null);

            // 处理actionHash：若签署已完成但哈希为空，说明数据不一致，从已有数据重新计算
            String actionHash = record.getActionHash();
            if (SignRecord.SignStatus.COMPLETED == record.getStatus()
                    && (actionHash == null || actionHash.isEmpty())) {
                log.warn("检测到数据不一致: 签署记录已完成但actionHash为空, contractId={}, signerId={}, recordId={}",
                        contract.getId(), record.getSignerId(), record.getId());
                // 根据签署数据重新计算哈希
                Signer signer = signerService.getSignerEntity(record.getSignerId());
                actionHash = HashUtil.combineHash(
                        contract.getContractNo(),
                        signer.getIdentityStamp(),
                        formatDateTime(record.getSignTime()),
                        record.getSignature() != null ? record.getSignature() : "",
                        record.getIpAddress() != null ? record.getIpAddress() : ""
                );
                log.info("重新计算actionHash: contractNo={}, signerId={}, newHash={}",
                        contract.getContractNo(), record.getSignerId(), actionHash);
            }
            signerInfo.put("actionHash", actionHash);
            signerInfo.put("signatureAlgorithm", record.getSignatureAlgorithm());
            signerInfo.put("deviceType", record.getDeviceType() != null ? record.getDeviceType().name() : null);
            signerInfo.put("ipAddress", record.getIpAddress());
            signerInfo.put("status", record.getStatus().name());
            signerInfo.put("fabricTxHash", record.getFabricTxHash());
            return signerInfo;
        }).collect(Collectors.toList());

        catalog.put("signRecords", signerInfoList);

        try {
            return objectMapper.writeValueAsString(catalog);
        } catch (JsonProcessingException e) {
            throw BusinessException.serverError("生成证据目录失败");
        }
    }

    /**
     * 转换为响应DTO
     */
    private EvidencePackageResponse toResponse(EvidencePackage evidencePackage) {
        Contract contract = contractRepository.findById(evidencePackage.getContractId()).orElse(null);
        
        return EvidencePackageResponse.builder()
                .id(evidencePackage.getId())
                .packageNo(evidencePackage.getPackageNo())
                .contract(contract != null ? new ContractService(null, null, null).getContractById(contract.getId()) : null)
                .caseNo(evidencePackage.getCaseNo())
                .type(evidencePackage.getType().name())
                .status(evidencePackage.getStatus().name())
                .evidenceCatalog(evidencePackage.getEvidenceCatalog())
                .packageHash(evidencePackage.getPackageHash())
                .credibilityScore(evidencePackage.getCredibilityScore())
                .chainVerifyStatus(evidencePackage.getChainVerifyStatus().name())
                .verifiedTime(evidencePackage.getVerifiedTime())
                .createdAt(evidencePackage.getCreatedAt())
                .expireTime(evidencePackage.getExpireTime())
                .build();
    }

}