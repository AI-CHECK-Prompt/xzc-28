package com.example.contract.service;

import com.example.contract.config.BlockchainConfig;
import com.example.contract.entity.Contract;
import com.example.contract.entity.SignRecord;
import com.example.contract.exception.BusinessException;
import com.example.contract.repository.ContractRepository;
import com.example.contract.repository.SignRecordRepository;
import com.example.contract.util.HashUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 区块链服务
 * 处理与区块链的交互，包括证据上链、验证等
 * 
 * <p>安全注意事项：
 * <ul>
 *   <li>使用 HashUtil.generateTransactionHash() 确保交易哈希的唯一性</li>
 *   <li>批量上链时传入记录ID进一步增强唯一性保证</li>
 *   <li>避免使用 UUID.randomUUID() 在高并发场景下的碰撞风险</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BlockchainService {

    private final BlockchainConfig blockchainConfig;
    private final ContractRepository contractRepository;
    private final SignRecordRepository signRecordRepository;

    /**
     * 存储证据到联盟链
     */
    @Transactional
    public String storeEvidence(String evidenceHash, Long contractId) {
        // 使用安全的交易哈希生成方法
        String txHash = simulateBlockchainTransaction(evidenceHash, contractId);
        
        // 更新合同存证信息
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> BusinessException.notFound("合同不存在"));
        contract.setFabricTxHash(txHash);
        contract.setEvidenceStatus(Contract.EvidenceStatus.SUCCESS);
        contractRepository.save(contract);

        log.info("证据上链成功: contractId={}, txHash={}", contractId, txHash);
        return txHash;
    }

    /**
     * 验证链上证据
     */
    public boolean verifyEvidence(String txHash, String expectedHash) {
        // 模拟链上验证
        String storedHash = retrieveFromBlockchain(txHash);
        return expectedHash.equals(storedHash);
    }

    /**
     * 存储证据到公有链（可选）
     */
    @Transactional
    public String storeEvidenceToEth(String evidenceHash, Long contractId) {
        // 使用安全的交易哈希生成方法
        String txHash = simulateEthTransaction(evidenceHash, contractId);
        
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> BusinessException.notFound("合同不存在"));
        contract.setEthTxHash(txHash);
        contractRepository.save(contract);

        log.info("证据上链到公有链成功: contractId={}, txHash={}", contractId, txHash);
        return txHash;
    }

    /**
     * 批量上链
     * 
     * <p>关键改进：
     * <ul>
     *   <li>使用 HashUtil.generateTransactionHash() 生成唯一交易哈希</li>
     *   <li>传入记录ID确保同一批次内每条记录的哈希唯一性</li>
     *   <li>避免短时间内多次调用导致的哈希碰撞</li>
     * </ul>
     */
    @Transactional
    public void batchStoreEvidence(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> BusinessException.notFound("合同不存在"));

        // 上链合同哈希（传入contractId增强唯一性）
        String contractTxHash = simulateBlockchainTransaction(contract.getContentHash(), contract.getId());
        contract.setFabricTxHash(contractTxHash);

        // 上链所有签署记录（传入recordId确保唯一性）
        signRecordRepository.findByContractIdOrderBySignOrder(contractId).forEach(record -> {
            if (record.getStatus() == SignRecord.SignStatus.COMPLETED && record.getFabricTxHash() == null) {
                // 传入记录ID，确保同一批次内每条记录的哈希唯一
                String recordTxHash = simulateBlockchainTransaction(record.getActionHash(), record.getId());
                record.setFabricTxHash(recordTxHash);
                signRecordRepository.save(record);
                
                log.debug("签署记录上链成功: recordId={}, txHash={}", record.getId(), recordTxHash);
            }
        });

        contractRepository.save(contract);
        log.info("批量上链完成: contractId={}", contractId);
    }

    /**
     * 跨链同步证据
     */
    public String crossChainSync(Long contractId, String targetChain) {
        // 使用安全的交易哈希生成方法
        String crossTxHash = "CROSS-" + HashUtil.nextSecureRandomHex(64);
        log.info("跨链同步成功: contractId={}, targetChain={}, crossTxHash={}", 
                contractId, targetChain, crossTxHash);
        return crossTxHash;
    }

    /**
     * 调用外部链验证接口（实际实现需替换为真实SDK调用）
     * 
     * @param contractId 合同ID
     * @param targetChain 目标链类型
     * @return 外部链返回的JSON响应
     */
    public String crossChainVerify(Long contractId, String targetChain) {
        // 模拟外部链验证响应
        // 实际实现应调用外部链的SDK或API
        // 这里根据contractId生成不同的响应，模拟不同验证结果
        Map<String, Object> response = new HashMap<>();
        
        // 模拟验证逻辑：某些contractId返回验证失败
        if (contractId != null && contractId % 3 == 0) {
            // 模拟验证失败：verified=false但返回成功状态
            response.put("verified", false);
            response.put("errorCode", "VERIFY_FAILED");
            response.put("errorMessage", "链上验证失败：证据哈希不匹配");
        } else if (contractId != null && contractId % 5 == 0) {
            // 模拟外部链异常：返回错误码
            response.put("verified", false);
            response.put("errorCode", "CHAIN_ERROR");
            response.put("errorMessage", "外部链节点通信超时");
        } else {
            // 模拟验证成功
            response.put("verified", true);
            response.put("errorCode", "0");
            response.put("chainTxHash", "CHAIN-TX-" + HashUtil.nextSecureRandomHex(32));
        }
        
        response.put("timestamp", System.currentTimeMillis());
        response.put("chainType", targetChain);
        
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            log.error("序列化外部链响应失败: {}", e.getMessage());
            return "{\"verified\":false,\"errorCode\":\"SERIALIZE_ERROR\",\"errorMessage\":\"响应序列化失败\"}";
        }
    }

    private ObjectMapper objectMapper;
    
    @org.springframework.beans.factory.annotation.Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 模拟区块链交易（实际实现需替换为真实SDK调用）
     * 
     * @param data 业务数据哈希
     * @param uniqueId 唯一标识（如合同ID、记录ID），用于增强唯一性
     * @return 唯一的交易哈希
     */
    private String simulateBlockchainTransaction(String data, Long uniqueId) {
        // 使用安全的哈希生成方法，结合业务数据和唯一标识
        String hash = HashUtil.generateTransactionHash(data, uniqueId);
        return "FAB-" + hash.substring(0, 60);
    }

    /**
     * 模拟以太坊交易
     * 
     * @param data 业务数据哈希
     * @param uniqueId 唯一标识（如合同ID、记录ID），用于增强唯一性
     * @return 唯一的以太坊交易哈希
     */
    private String simulateEthTransaction(String data, Long uniqueId) {
        // 使用安全的哈希生成方法
        String hash = HashUtil.generateTransactionHash(data, uniqueId);
        return "0x" + hash;
    }

    /**
     * 模拟从区块链检索数据
     */
    private String retrieveFromBlockchain(String txHash) {
        // 模拟检索，实际应调用区块链SDK
        return txHash.substring(4); // 返回部分哈希作为模拟数据
    }

}