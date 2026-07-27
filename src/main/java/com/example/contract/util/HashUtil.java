package com.example.contract.util;

import org.apache.commons.codec.digest.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * 哈希工具类
 */
public class HashUtil {

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
        return sha256(idCard + timestamp);
    }

    /**
     * 生成合同编号
     */
    public static String generateContractNo() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = String.valueOf(Math.random()).substring(2, 10);
        return "CON" + timestamp + random;
    }

    /**
     * 生成证据包编号
     */
    public static String generatePackageNo() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = String.valueOf(Math.random()).substring(2, 10);
        return "EVP" + timestamp + random;
    }

}