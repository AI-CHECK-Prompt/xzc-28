package com.example.contract.util;

import org.apache.commons.codec.digest.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 哈希工具类
 * 
 * <p>安全注意事项：
 * <ul>
 *   <li>使用 SecureRandom 替代 Math.random() 以防止随机数生成器种子碰撞</li>
 *   <li>交易哈希生成时引入纳秒时间戳、线程ID、原子计数器等多重唯一标识</li>
 *   <li>支持分布式环境下的唯一性保证</li>
 * </ul>
 */
public class HashUtil {

    /**
     * 安全随机数生成器
     */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 原子计数器，用于确保批量处理时的唯一性
     */
    private static final AtomicLong COUNTER = new AtomicLong(0);

    /**
     * 计算字符串的SHA-256哈希值
     */
    public static String sha256(String input) {
        if (input == null) {
            return null;
        }
        return DigestUtils.sha256Hex(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 计算字节数组的SHA-256哈希值
     */
    public static String sha256(byte[] input) {
        if (input == null) {
            return null;
        }
        return DigestUtils.sha256Hex(input);
    }

    /**
     * 计算多个字符串的组合哈希值
     */
    public static String combineHash(String... inputs) {
        StringBuilder combined = new StringBuilder();
        for (String input : inputs) {
            if (input != null) {
                combined.append(input);
            }
        }
        return sha256(combined.toString());
    }

    /**
     * 生成身份戳（基于身份证号和时间戳）
     */
    public static String generateIdentityStamp(String idCard) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String nanoTime = String.valueOf(System.nanoTime());
        return sha256(idCard + timestamp + nanoTime + nextSecureRandomHex(8));
    }

    /**
     * 生成合同编号
     */
    public static String generateContractNo() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String nanoTime = String.valueOf(System.nanoTime());
        String random = nextSecureRandomHex(8);
        return "CON" + timestamp + nanoTime.substring(0, 6) + random;
    }

    /**
     * 生成证据包编号
     */
    public static String generatePackageNo() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String nanoTime = String.valueOf(System.nanoTime());
        String random = nextSecureRandomHex(8);
        return "EVP" + timestamp + nanoTime.substring(0, 6) + random;
    }

    /**
     * 生成交易哈希（用于区块链交易）
     * 
     * @param data 业务数据哈希
     * @param recordId 记录ID（可选，用于更强的唯一性保证）
     * @return 唯一的交易哈希
     */
    public static String generateTransactionHash(String data, Long recordId) {
        StringBuilder sb = new StringBuilder();
        sb.append(data != null ? data : "");
        sb.append(System.currentTimeMillis());      // 毫秒时间戳
        sb.append(System.nanoTime());               // 纳秒时间戳
        sb.append(Thread.currentThread().getId());  // 线程ID
        sb.append(COUNTER.incrementAndGet());       // 原子计数器
        sb.append(nextSecureRandomHex(16));         // 安全随机数
        
        if (recordId != null) {
            sb.append(recordId);                    // 记录ID（如果提供）
        }
        
        return sha256(sb.toString());
    }

    /**
     * 生成交易哈希（无记录ID版本）
     */
    public static String generateTransactionHash(String data) {
        return generateTransactionHash(data, null);
    }

    /**
     * 使用 SecureRandom 生成指定长度的十六进制随机字符串
     * 
     * @param length 输出长度（字符数）
     * @return 十六进制随机字符串
     */
    public static String nextSecureRandomHex(int length) {
        if (length <= 0) {
            return "";
        }
        
        int bytesNeeded = (length + 1) / 2;
        byte[] bytes = new byte[bytesNeeded];
        SECURE_RANDOM.nextBytes(bytes);
        
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        
        return sb.toString().substring(0, length);
    }

    /**
     * 使用 ThreadLocalRandom 生成整数随机数（性能更好，适用于非安全场景）
     * 
     * @param min 最小值（包含）
     * @param max 最大值（不包含）
     * @return 随机整数
     */
    public static int nextInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max);
    }

}