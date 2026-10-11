package com.group7.shop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;

/**
 * 购买意向共享实体。
 *
 * <p>口令码生成、查询、修改和取消由意向域负责人继续实现；交易域只依赖
 * productId、status 以及下面的状态流转方法，避免保存或返回明文口令码。</p>
 */
@Entity
@Table(name = "purchase_intents")
public class Intent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String phone;

    @Column(nullable = false, unique = true, length = 128)
    private String codeHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IntentStatus status = IntentStatus.WAITING;

    @Column(nullable = false, updatable = false)
    private Instant submittedAt;

    private Instant finishedAt;

    @Version
    private Long version;

    protected Intent() {
    }

    public Intent(Long productId, String name, String phone, String codeHash) {
        this.productId = productId;
        this.name = name;
        this.phone = phone;
        this.codeHash = codeHash;
        this.status = IntentStatus.WAITING;
    }

    @PrePersist
    protected void onCreate() {
        if (submittedAt == null) {
            submittedAt = Instant.now();
        }
        if (status == null) {
            status = IntentStatus.WAITING;
        }
    }

    public void startTrade() {
        this.status = IntentStatus.IN_TRADE;
        this.finishedAt = null;
    }

    public void markTraded() {
        this.status = IntentStatus.TRADED;
        this.finishedAt = Instant.now();
    }

    public void markFailed() {
        this.status = IntentStatus.FAILED;
        this.finishedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getCodeHash() { return codeHash; }
    public IntentStatus getStatus() { return status; }
    public Instant getSubmittedAt() { return submittedAt; }
    public Instant getFinishedAt() { return finishedAt; }
    public Long getVersion() { return version; }
}
