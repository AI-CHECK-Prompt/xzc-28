package com.example.contract.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建签约方请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignerCreateRequest {

    /**
     * 签约方名称
     */
    @NotBlank(message = "签约方名称不能为空")
    private String name;

    /**
     * 身份证号（个人）或统一社会信用代码（企业）
     */
    @NotBlank(message = "证件号码不能为空")
    private String idCard;

    /**
     * 签约方类型：INDIVIDUAL(个人), ENTERPRISE(企业), ORGANIZATION(机构)
     */
    @NotBlank(message = "签约方类型不能为空")
    private String type;

    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 邮箱
     */
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "邮箱格式不正确")
    private String email;

}