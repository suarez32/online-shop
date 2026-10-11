package com.group7.shop.service;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * 口令码生成与哈希。
 * 规则（Wiki《接口设计》）：8 位大写字母数字，剔除易混字符 I / O / 0 / 1，
 * 全库唯一；口令码明文不落库，只保存 SHA-256 哈希。
 * 字符集与前端 common.js 的 CODE_PATTERN 保持一致。
 */
@Component
public class IntentCodeGenerator {

    /** A-H、J-N、P-Z 共 24 个字母 + 数字 2-9，共 32 个字符。 */
    public static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    public static final int CODE_LENGTH = 8;

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    /** SHA-256 十六进制（64 位小写），作为 codeHash 唯一索引列。 */
    public String hash(String code) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(code.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            // JDK 标准算法，理论上不可能缺失
            throw new IllegalStateException("SHA-256 算法不可用", e);
        }
    }
}
