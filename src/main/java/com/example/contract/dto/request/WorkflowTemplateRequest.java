package com.example.contract.dto.request;

import lombok.Data;
@Data
public class WorkflowTemplateRequest {
    private String name;
    private String description;
    private String category;
    private String bpmnXml;
    private String nodeConfigJson;
}