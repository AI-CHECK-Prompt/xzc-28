package com.example.contract.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 签约方响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignerResponse {

    /**
     * 签约方ID
     */
    private Long id;

    /**
     * 签约方名称
     */
    private String name;

    /**
     * 身份证号（脱敏显示）
     */
    private String idCardMasked;

    /**
     * 签约方类型
     */
    private String type;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 实名认证状态
     */
    private String authStatus;

    /**
     * 认证级别
     */
    private String authLevel;

    /**
     * 身份戳
     */
    private String identityStamp;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

}