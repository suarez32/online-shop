package com.group7.shop.dto;

import com.group7.shop.entity.Intent;
import com.group7.shop.entity.IntentStatus;

import java.time.Instant;

/**
 * 买家侧意向视图（意向域负责人：华天遥）。
 * 与 Wiki GET /api/intents/{code} 出参一致：{ name, phone, status, position, submittedAt }。
 * position：排队中为该商品 WAITING 意向中的位次（从 1 开始）；已进入交易时置 null。
 * 刻意不含 id 与口令码（codeHash），保护买家隐私。
 * 注意与账号域（李宇轩）的 IntentView 区分：那份含 id，用于卖家后台总览/选定买家。
 */
public record BuyerIntentView(
        String name,
        String phone,
        IntentStatus status,
        Integer position,
        Instant submittedAt
) {
    public static BuyerIntentView from(Intent intent, Integer position) {
        return new BuyerIntentView(
                intent.getName(),
                intent.getPhone(),
                intent.getStatus(),
                position,
                intent.getSubmittedAt()
        );
    }
}
