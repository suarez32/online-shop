package com.group7.shop.dto;

import com.group7.shop.dto.ProductView;

import java.util.List;

/**
 * 后台首页聚合数据：当前在售/交易中商品 + 意向购买人列表（按提交时间正序，含 position）+ 是否可发布新品。
 */
public record SellerOverview(
        ProductView product,
        List<IntentView> queue,
        boolean canPublish
) {
}
