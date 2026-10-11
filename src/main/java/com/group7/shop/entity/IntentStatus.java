package com.group7.shop.entity;

/**
 * 购买意向状态机：
 * WAITING 排队中 -> IN_TRADE 交易中（卖家选定） -> TRADED 交易成功 / FAILED 交易失败
 * 任意终态可由 CANCELLED 已撤销进入。
 */
public enum IntentStatus {
    WAITING,
    IN_TRADE,
    TRADED,
    FAILED,
    CANCELLED
}
