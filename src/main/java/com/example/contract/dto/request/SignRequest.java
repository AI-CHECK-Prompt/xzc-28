package com.example.contract.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 签署请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignRequest {

    /**
     * 合同编号
     */
    @NotBlank(message = "合同编号不能为空")
    private String contractNo;

    /**
     * 签约方ID
     */
    @NotNull(message = "签约方ID不能为空")
    private Long signerId;

    /**
     * 签署类型：INITIAL(初始签署), COUNTERSIGN(会签), WITNESS(见证), NOTARY(公证)
     */
    private String signType;

    /**
     * 数字签名值（Base64编码）
     */
    @NotBlank(message = "数字签名不能为空")
    private String signatureBase64;

    /**
     * 签名算法：RSA, ECDSA, SM2
     */
    private String signatureAlgorithm;

    /**
     * 证书链（Base64编码，可选）
     */
    private String certificateChainBase64;

    /**
     * 设备类型：PC, MOBILE, TABLET, SERVER
     */
    private String deviceType;

    /**
     * 设备型号
     */
    private String deviceModel;

    /**
     * 操作系统
     */
    private String os;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 网络类型
     */
    private String networkType;

    /**
     * 地理位置（经纬度）
     */
    private String location;

    /**
     * 浏览器信息
     */
    private String userAgent;

}