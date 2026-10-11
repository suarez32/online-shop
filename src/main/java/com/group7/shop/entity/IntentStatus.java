package com.group7.shop.entity;

/** 购买意向状态，与 Wiki《接口设计》的状态机保持一致。 */
public enum IntentStatus {
    WAITING,
    IN_TRADE,
    TRADED,
    FAILED,
    CANCELLED
}
