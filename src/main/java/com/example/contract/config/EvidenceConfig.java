package com.example.contract.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 存证配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "evidence")
public class EvidenceConfig {

    /**
     * 文件存储路径
     */
    private String storagePath = "./data/storage";

    /**
     * 最大文件大小（字节）
     */
    private Long maxFileSize = 10485760L;

    /**
     * 哈希算法
     */
    private String hashAlgorithm = "SHA-256";

}