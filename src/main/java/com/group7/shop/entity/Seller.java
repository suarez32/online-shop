package com.group7.shop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 卖家账号。密码以「随机盐 + SHA-256」哈希存储（见 {@link com.group7.shop.common.PasswordHasher}），
 * 不存明文、不存可逆哈希；salt 列与 passwordHash 中的前缀盐保持一致。
 * token 为当前有效登录凭证，重新登录 / 登出 / 改密后旧 token 即失效。
 */
@Entity
@Table(name = "sellers")
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 64)
    private String salt;

    /** 当前有效 token；登出或改密后置 null，旧 token 即失效。 */
    @Column(length = 64)
    private String token;

    protected Seller() {
    }

    public Seller(String username, String salt, String passwordHash, String token) {
        this.username = username;
        this.salt = salt;
        this.passwordHash = passwordHash;
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
