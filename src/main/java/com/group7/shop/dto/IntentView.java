package com.group7.shop.dto;

import com.group7.shop.entity.IntentStatus;

import java.time.Instant;

/**
 * 后台总览中的意向购买人视图：含姓名/电话/状态/位次/提交时间，
 * 刻意不含口令码（codeHash），保护买家隐私。
 */
public record IntentView(
        Long id,
        String name,
        String phone,
        IntentStatus status,
        Integer position,
        Instant submittedAt
) {
}
