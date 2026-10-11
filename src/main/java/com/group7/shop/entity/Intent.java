package com.group7.shop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;

/**
 * 买家购买意向。口令码只存哈希（codeHash），明文仅在提交响应返回一次。
 * 本实体为冻结数据模型的一部分；写侧（提交/查询/修改/撤销）由意向域（华天遥）实现，
 * 账号与后台域（李宇轩）仅按 productId + submittedAt 只读聚合到后台总览。
 */
@Entity
@Table(name = "intents")
public class Intent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(nullable = false, length = 32)
    private String phone;

    /** 口令码哈希，仅存哈希；明文只在提交响应返回一次。 */
    @Column(nullable = false, length = 64)
    private String codeHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IntentStatus status = IntentStatus.WAITING;

    @Column(nullable = false)
    private Instant submittedAt;

    @Version
    private Long version;

    protected Intent() {
    }

    public Intent(Long productId, String name, String phone, String codeHash, IntentStatus status, Instant submittedAt) {
        this.productId = productId;
        this.name = name;
        this.phone = phone;
        this.codeHash = codeHash;
        this.status = status == null ? IntentStatus.WAITING : status;
        this.submittedAt = submittedAt == null ? Instant.now() : submittedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getCodeHash() {
        return codeHash;
    }

    public IntentStatus getStatus() {
        return status;
    }

    public void setStatus(IntentStatus status) {
        this.status = status;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public Long getVersion() {
        return version;
    }
}
