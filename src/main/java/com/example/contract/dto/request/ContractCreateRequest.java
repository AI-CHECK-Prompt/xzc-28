package com.example.contract.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建合同请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractCreateRequest {

    /**
     * 合同名称
     */
    @NotBlank(message = "合同名称不能为空")
    private String name;

    /**
     * 合同类型：SERVICE(服务合同), SALES(销售合同), LEASE(租赁合同), EMPLOYMENT(劳动合同), OTHER(其他)
     */
    @NotBlank(message = "合同类型不能为空")
    private String type;

    /**
     * 合同模板编号（可选）
     */
    private String templateCode;

    /**
     * 合同内容（Base64编码）
     */
    @NotBlank(message = "合同内容不能为空")
    private String contentBase64;

    /**
     * 签署方ID列表
     */
    @NotNull(message = "签署方列表不能为空")
    private List<Long> signerIds;

    /**
     * 签署截止时间
     */
    private LocalDateTime signDeadline;

    /**
     * 合同生效时间
     */
    private LocalDateTime effectiveTime;

    /**
     * 合同到期时间
     */
    private LocalDateTime expireTime;

    /**
     * 创建人ID
     */
    @NotNull(message = "创建人ID不能为空")
    private Long creatorId;

}