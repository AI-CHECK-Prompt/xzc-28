package com.example.contract.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WorkflowTemplateResponse {
    private Long id;
    private String name;
    private String description;
    private String category;
    private Integer currentVersion;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}