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

        // 调用外部链验证接口获取响应
        Map<String, Object> chainResponse = callExternalChainVerifyApi(contract, targetChainEnum);

        // 解析外部链返回的业务状态码
        boolean verified = parseVerifyResult(chainResponse);

        // 记录验证操作
        CrossChainRecord record = CrossChainRecord.builder()
                .contractId(contractId)
                .type(CrossChainRecord.CrossChainType.VERIFY)
                .targetChain(targetChainEnum)
                .responseData(toJson(chainResponse))
                .status(verified ? CrossChainRecord.CrossChainStatus.SUCCESS : CrossChainRecord.CrossChainStatus.FAILED)
                .retryCount(0)
                .completedAt(LocalDateTime.now())
                .build();

        // 如果验证失败，记录错误信息
        if (!verified && chainResponse.containsKey("errorMessage")) {
            record.setErrorMessage(String.valueOf(chainResponse.get("errorMessage")));
        }

        crossChainRecordRepository.save(record);
        log.info("跨链验证完成: contractId={}, targetChain={}, verified={}", contractId, targetChain, verified);

        return verified;
    }

    /**
     * 调用外部链验证API
     */
    private Map<String, Object> callExternalChainVerifyApi(Contract contract, CrossChainRecord.TargetChain targetChain) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 构建验证请求参数
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("contractId", contract.getId());
            requestParams.put("contractNo", contract.getContractNo());
            requestParams.put("contentHash", contract.getContentHash());
            requestParams.put("targetChain", targetChain.name());
            requestParams.put("requestTime", LocalDateTime.now().toString());

            // 调用外部链验证接口（实际实现需替换为真实SDK调用）
            // 这里模拟外部链返回的响应
            String chainResponse = blockchainService.crossChainVerify(contract.getId(), targetChain.name());
            if (chainResponse != null && !chainResponse.isEmpty()) {
                response = objectMapper.readValue(chainResponse, Map.class);
            } else {
                response.put("verified", false);
                response.put("errorMessage", "外部链返回空响应");
                response.put("errorCode", "EMPTY_RESPONSE");
            }
        } catch (Exception e) {
            log.error("调用外部链验证API异常: contractId={}, targetChain={}, error={}",
                    contract.getId(), targetChain, e.getMessage());
            response.put("verified", false);
            response.put("errorMessage", "外部链验证异常: " + e.getMessage());
            response.put("errorCode", "API_ERROR");
        }
        return response;
    }

    /**
     * 解析外部链返回的验证结果
     * 检查响应中的业务状态码，而非仅检查响应是否为空
     */
    private boolean parseVerifyResult(Map<String, Object> chainResponse) {
        if (chainResponse == null || chainResponse.isEmpty()) {
            log.warn("外部链返回空响应，验证结果视为失败");
            return false;
        }

        // 解析业务状态码字段
        if (chainResponse.containsKey("verified")) {
            Object verifiedValue = chainResponse.get("verified");
            if (verifiedValue instanceof Boolean) {
                return (Boolean) verifiedValue;
            } else if (verifiedValue instanceof String) {
                return Boolean.parseBoolean((String) verifiedValue);
            }
        }

        // 检查错误码字段
        if (chainResponse.containsKey("errorCode")) {
            String errorCode = String.valueOf(chainResponse.get("errorCode"));
            if (!"0".equals(errorCode) && !"SUCCESS".equalsIgnoreCase(errorCode)) {
                log.warn("外部链返回错误码: {}", errorCode);
                return false;
            }
        }

        // 检查业务状态字段
        if (chainResponse.containsKey("status")) {
            String status = String.valueOf(chainResponse.get("status"));
            if ("FAILED".equalsIgnoreCase(status) || "FAIL".equalsIgnoreCase(status) || "ERROR".equalsIgnoreCase(status)) {
                log.warn("外部链返回业务状态: {}", status);
                return false;
            }
        }

        return false;
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