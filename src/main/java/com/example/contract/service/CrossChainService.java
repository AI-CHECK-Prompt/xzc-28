package com.example.contract.service;

import com.example.contract.entity.Contract;
import com.example.contract.entity.CrossChainRecord;
import com.example.contract.exception.BusinessException;
import com.example.contract.repository.ContractRepository;
import com.example.contract.repository.CrossChainRecordRepository;
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

/**
 * 跨链协同服务
 * 支持与外部司法链的双向互通
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CrossChainService {

    private final CrossChainRecordRepository crossChainRecordRepository;
    private final ContractRepository contractRepository;
    private final BlockchainService blockchainService;
    private final ObjectMapper objectMapper;

    /**
     * 同步证据到外部链
     */
    @Transactional
    public String syncToExternalChain(Long contractId, String targetChain) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> BusinessException.notFound("合同不存在"));

        // 验证目标链类型
        CrossChainRecord.TargetChain targetChainEnum = parseTargetChain(targetChain);

        // 构建请求数据
        String requestData = buildRequestData(contract);

        // 执行跨链同步
        String crossTxHash = blockchainService.crossChainSync(contractId, targetChain);

        // 记录跨链操作
        CrossChainRecord record = CrossChainRecord.builder()
                .contractId(contractId)
                .type(CrossChainRecord.CrossChainType.SYNC)
                .targetChain(targetChainEnum)
                .requestData(requestData)
                .crossChainTxHash(crossTxHash)
                .status(CrossChainRecord.CrossChainStatus.SUCCESS)
                .retryCount(0)
                .completedAt(LocalDateTime.now())
                .build();

        crossChainRecordRepository.save(record);
        log.info("跨链同步成功: contractId={}, targetChain={}, crossTxHash={}", 
                contractId, targetChain, crossTxHash);

        return crossTxHash;
    }

    /**
     * 从外部链查询证据
     */
    public Map<String, Object> queryFromExternalChain(Long contractId, String targetChain) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> BusinessException.notFound("合同不存在"));

        CrossChainRecord.TargetChain targetChainEnum = parseTargetChain(targetChain);

        // 模拟查询响应
        Map<String, Object> response = new HashMap<>();
        response.put("contractNo", contract.getContractNo());
        response.put("contentHash", contract.getContentHash());
        response.put("evidenceExists", true);
        response.put("verified", true);
        response.put("queryTime", LocalDateTime.now().toString());

        // 记录查询操作
        CrossChainRecord record = CrossChainRecord.builder()
                .contractId(contractId)
                .type(CrossChainRecord.CrossChainType.QUERY)
                .targetChain(targetChainEnum)
                .responseData(toJson(response))
                .status(CrossChainRecord.CrossChainStatus.SUCCESS)
                .retryCount(0)
                .completedAt(LocalDateTime.now())
                .build();

        crossChainRecordRepository.save(record);
        log.info("跨链查询成功: contractId={}, targetChain={}", contractId, targetChain);

        return response;
    }

    /**
     * 验证跨链证据
     */
    public boolean verifyCrossChainEvidence(Long contractId, String targetChain) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> BusinessException.notFound("合同不存在"));

        CrossChainRecord.TargetChain targetChainEnum = parseTargetChain(targetChain);

        // 模拟验证
        boolean verified = true;

        // 记录验证操作
        CrossChainRecord record = CrossChainRecord.builder()
                .contractId(contractId)
                .type(CrossChainRecord.CrossChainType.VERIFY)
                .targetChain(targetChainEnum)
                .responseData("{\"verified\":" + verified + "}")
                .status(verified ? CrossChainRecord.CrossChainStatus.SUCCESS : CrossChainRecord.CrossChainStatus.FAILED)
                .retryCount(0)
                .completedAt(LocalDateTime.now())
                .build();

        crossChainRecordRepository.save(record);
        log.info("跨链验证完成: contractId={}, targetChain={}, verified={}", contractId, targetChain, verified);

        return verified;
    }

    /**
     * 获取合同的跨链记录
     */
    public List<Map<String, Object>> getCrossChainRecords(Long contractId) {
        return crossChainRecordRepository.findByContractIdOrderByCreatedAtDesc(contractId).stream()
                .map(record -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", record.getId());
                    map.put("type", record.getType().name());
                    map.put("targetChain", record.getTargetChain().name());
                    map.put("status", record.getStatus().name());
                    map.put("crossTxHash", record.getCrossChainTxHash());
                    map.put("createdAt", record.getCreatedAt().toString());
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * 解析目标链类型
     */
    private CrossChainRecord.TargetChain parseTargetChain(String targetChain) {
        try {
            return CrossChainRecord.TargetChain.valueOf(targetChain.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw BusinessException.badRequest("无效的目标链类型: " + targetChain);
        }
    }

    /**
     * 构建请求数据
     */
    private String buildRequestData(Contract contract) {
        Map<String, Object> data = new HashMap<>();
        data.put("contractNo", contract.getContractNo());
        data.put("contentHash", contract.getContentHash());
        data.put("fabricTxHash", contract.getFabricTxHash());
        data.put("ethTxHash", contract.getEthTxHash());
        data.put("evidenceStatus", contract.getEvidenceStatus().name());
        data.put("syncTime", LocalDateTime.now().toString());
        return toJson(data);
    }

    /**
     * 对象转JSON字符串
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw BusinessException.serverError("JSON序列化失败");
        }
    }

}