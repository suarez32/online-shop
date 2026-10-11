package com.group7.shop.entity;

/**
 * 购买意向状态机，取值与 Wiki《接口设计》一致：
 * WAITING 排队中 / IN_TRADE 交易中 / TRADED 交易成功 / FAILED 交易失败 / CANCELLED 已撤销。
 * TRADED、FAILED、CANCELLED 为终态，终态意向不可再查询、修改或撤销。
 */
public enum IntentStatus {
    WAITING,
    IN_TRADE,
    TRADED,
    FAILED,
    CANCELLED;

    /** 终态判定：已成功/失败/撤销的意向，凭码操作一律 404。 */
    public boolean isTerminal() {
        return this == TRADED || this == FAILED || this == CANCELLED;
    }
}
