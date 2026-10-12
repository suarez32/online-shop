package com.group7.shop.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 口令码生成器单元测试（负责人：华天遥）。
 * 规则来源：Wiki《接口设计》——8 位大写字母数字，剔除 I / O / 0 / 1，全库唯一。
 */
class IntentCodeGeneratorTest {

    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-HJ-NP-Z2-9]{8}$");
    private static final Pattern SHA256_HEX = Pattern.compile("^[0-9a-f]{64}$");

    private final IntentCodeGenerator generator = new IntentCodeGenerator();

    @Test
    @DisplayName("TC-C01 生成的口令码均为 8 位、不含 I/O/0/1")
    void codeFormat() {
        for (int i = 0; i < 1000; i++) {
            String code = generator.generate();
            assertTrue(CODE_PATTERN.matcher(code).matches(),
                    "口令码格式非法: " + code);
        }
    }

    @Test
    @DisplayName("TC-C02 连续生成 1000 个口令码无重复（全库唯一的基本保障）")
    void codeUniqueness() {
        Set<String> codes = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            codes.add(generator.generate());
        }
        assertEquals(1000, codes.size(), "口令码出现重复");
    }

    @Test
    @DisplayName("TC-C03 哈希为 64 位十六进制且同一口令码结果稳定")
    void hashStable() {
        String code = "ABCDEFGH";
        String hash1 = generator.hash(code);
        String hash2 = generator.hash(code);
        assertTrue(SHA256_HEX.matcher(hash1).matches(), "哈希格式非法: " + hash1);
        assertEquals(hash1, hash2, "同一口令码两次哈希结果不一致");
        assertEquals(64, hash1.length());
    }

    @Test
    @DisplayName("TC-C04 不同口令码哈希不同")
    void hashDifferentForDifferentCodes() {
        assertNotEquals(generator.hash("ABCDEFGH"), generator.hash("ABCDEFGJ"));
    }
}
