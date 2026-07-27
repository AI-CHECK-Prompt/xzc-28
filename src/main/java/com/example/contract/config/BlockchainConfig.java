package com.example.contract.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 区块链配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "blockchain")
public class BlockchainConfig {

    /**
     * Ethereum配置
     */
    private EthereumConfig ethereum = new EthereumConfig();

    /**
     * Fabric配置
     */
    private FabricConfig fabric = new FabricConfig();

    @Data
    public static class EthereumConfig {
        /**
         * RPC节点地址
         */
        private String rpcUrl;

        /**
         * 合约地址
         */
        private String contractAddress;

        /**
         * 私钥
         */
        private String privateKey;
    }

    @Data
    public static class FabricConfig {
        /**
         * 通道名称
         */
        private String channelName;

        /**
         * 链码名称
         */
        private String chaincodeName;

        /**
         * MSP ID
         */
        private String mspId;

        /**
         * 连接配置文件路径
         */
        private String connectionProfile;
    }

}