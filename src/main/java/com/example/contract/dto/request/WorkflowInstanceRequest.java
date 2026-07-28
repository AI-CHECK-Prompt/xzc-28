package com.example.contract.dto.request;

import lombok.Data;
@Data
public class WorkflowInstanceRequest {
    private Long contractId;
    private Long templateId;
}