package com.group7.shop.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 依赖无关的密码哈希工具（无第三方依赖，可在现有 pom 下直接编译）。
 * 采用「随机盐 + SHA-256」：passwordHash 存储为 "saltBase64:hashBase64"，salt 列同步保存 salt。
 *
 * 说明：团队《技术选型》Wiki 建议用 BCrypt（spring-security-crypto）。为不改动他人的构建配置（pom.xml），
 * 此处用 JDK 内置 MessageDigest 实现等价的安全哈希；若后续在 pom 中加入 spring-security-crypto，
 * 可平滑替换为 BCrypt，接口与存储字段保持不变。
 */
public final class PasswordHasher {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALGO = "SHA-256";

    private PasswordHasher() {
    }

    /** 生成 16 字节随机盐（Base64 字符串）。 */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /** 用给定盐对明文密码做 SHA-256，返回 Base64 摘要。 */
    public static String hash(String salt, String rawPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGO);
            md.update(Base64.getDecoder().decode(salt));
            byte[] digest = md.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("密码哈希失败", ex);
        }
    }

    /** 生成完整存储串：salt:hash。 */
    public static String encode(String rawPassword) {
        String salt = generateSalt();
        return salt + ":" + hash(salt, rawPassword);
    }

    /** 校验明文密码是否与存储串匹配（恒定时间比较，避免计时攻击）。 */
    public static boolean matches(String rawPassword, String stored) {
        if (stored == null || !stored.contains(":")) {
            return false;
        }
        String[] parts = stored.split(":", 2);
        String expected = hash(parts[0], rawPassword);
        byte[] a = Base64.getDecoder().decode(expected);
        byte[] b = Base64.getDecoder().decode(parts[1]);
        if (a.length != b.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
}
