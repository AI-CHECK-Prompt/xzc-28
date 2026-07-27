package com.example.contract.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 签署记录响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignRecordResponse {

    /**
     * 签署记录ID
     */
    private Long id;

    /**
     * 合同编号
     */
    private String contractNo;

    /**
     * 签约方信息
     */
    private SignerResponse signer;

    /**
     * 签署顺序
     */
    private Integer signOrder;

    /**
     * 签署类型
     */
    private String signType;

    /**
     * 签署状态
     */
    private String status;

    /**
     * 签署时间
     */
    private LocalDateTime signTime;

    /**
     * 时间戳权威来源
     */
    private String timeSource;

    /**
     * 签署行为哈希值
     */
    private String actionHash;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 签名算法
     */
    private String signatureAlgorithm;

    /**
     * 链上交易哈希
     */
    private String fabricTxHash;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

}