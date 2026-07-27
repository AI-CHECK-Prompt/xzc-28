package com.example.contract.service;

import com.example.contract.entity.Contract;
import com.example.contract.entity.Signer;
import com.example.contract.entity.SignRecord;
import com.example.contract.repository.ContractRepository;
import com.example.contract.repository.SignerRepository;
import com.example.contract.repository.SignRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 证据可信度评估服务
 * 根据多因子综合计算证据可信度评分
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CredibilityService {

    private final ContractRepository contractRepository;
    private final SignRecordRepository signRecordRepository;
    private final SignerRepository signerRepository;

    /**
     * 计算证据可信度评分（0-100）
     */
    public Integer calculateCredibility(Long contractId) {
        Contract contract = contractRepository.findById(contractId).orElse(null);
        if (contract == null) {
            return 0;
        }

        int score = 0;

        // 链式存储完整性评分（权重30%）
        int chainScore = evaluateChainStorage(contract);
        score += chainScore * 3 / 10;

        // 签署主体认证强度评分（权重30%）
        int authScore = evaluateSignerAuthentication(contractId);
        score += authScore * 3 / 10;

        // 时间戳权威性评分（权重20%）
        int timeScore = evaluateTimestampAuthority(contractId);
        score += timeScore * 2 / 10;

        // 合同内容完整性评分（权重20%）
        int contentScore = evaluateContentIntegrity(contract);
        score += contentScore * 2 / 10;

        log.info("计算证据可信度评分: contractId={}, score={}", contractId, score);
        return score;
    }

    /**
     * 评估链式存储完整性
     */
    private int evaluateChainStorage(Contract contract) {
        int score = 0;

        // 联盟链存证
        if (contract.getFabricTxHash() != null && !contract.getFabricTxHash().isEmpty()) {
            score += 50;
        }

        // 公有链存证（额外加分）
        if (contract.getEthTxHash() != null && !contract.getEthTxHash().isEmpty()) {
            score += 30;
        }

        // 存证状态
        if (contract.getEvidenceStatus() == Contract.EvidenceStatus.SUCCESS) {
            score += 20;
        }

        return Math.min(score, 100);
    }

    /**
     * 评估签署主体认证强度
     * 使用签署时的认证级别快照进行评分，避免评分与存证时间窗口耦合
     */
    private int evaluateSignerAuthentication(Long contractId) {
        List<SignRecord> signRecords = signRecordRepository.findByContractIdOrderBySignOrder(contractId);
        
        if (signRecords.isEmpty()) {
            return 0;
        }

        int totalScore = 0;
        int count = 0;

        for (SignRecord record : signRecords) {
            // 使用签署时的认证级别快照进行评分
            int score = evaluateSignerLevelSnapshot(record.getSignerAuthStatus(), record.getSignerAuthLevel());
            if (score > 0) {
                count++;
                totalScore += score;
            }
        }

        return count > 0 ? totalScore / count : 0;
    }

    /**
     * 根据签署时的认证状态和级别评估评分（时间归一化处理）
     */
    private int evaluateSignerLevelSnapshot(Signer.AuthStatus authStatus, Signer.AuthLevel authLevel) {
        if (authStatus != Signer.AuthStatus.VERIFIED || authLevel == null) {
            return 0;
        }

        switch (authLevel) {
            case LEVEL4: // CA证书认证
                return 100;
            case LEVEL3: // 人脸核验
                return 80;
            case LEVEL2: // 实名认证
                return 60;
            case LEVEL1: // 基础认证
                return 30;
            default:
                return 0;
        }
    }

    /**
     * 评估时间戳权威性
     */
    private int evaluateTimestampAuthority(Long contractId) {
        List<SignRecord> signRecords = signRecordRepository.findByContractIdOrderBySignOrder(contractId);
        
        if (signRecords.isEmpty()) {
            return 0;
        }

        int totalScore = 0;
        int count = 0;

        for (SignRecord record : signRecords) {
            if (record.getSignTime() != null) {
                count++;
                totalScore += evaluateTimeSource(record.getTimeSource());
            }
        }

        return count > 0 ? totalScore / count : 0;
    }

    /**
     * 评估时间源权威性
     */
    private int evaluateTimeSource(SignRecord.TimeSource timeSource) {
        if (timeSource == null) {
            return 0;
        }

        switch (timeSource) {
            case BLOCKCHAIN: // 区块链时间
                return 100;
            case GPS: // 卫星时间
                return 90;
            case CA: // 证书时间
                return 80;
            case NTP: // 网络时间协议
                return 60;
            default:
                return 0;
        }
    }

    /**
     * 评估合同内容完整性
     */
    private int evaluateContentIntegrity(Contract contract) {
        int score = 0;

        // 合同内容哈希存在
        if (contract.getContentHash() != null && !contract.getContentHash().isEmpty()) {
            score += 50;
        }

        // 合同已签署完成
        if (contract.getStatus() == Contract.ContractStatus.SIGNED) {
            score += 30;
        }

        // 签署时间合理（非过期签署）
        if (contract.getLastSignTime() != null && contract.getSignDeadline() != null) {
            if (contract.getLastSignTime().isBefore(contract.getSignDeadline())) {
                score += 20;
            }
        }

        return Math.min(score, 100);
    }

}