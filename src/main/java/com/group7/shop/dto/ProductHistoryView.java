package com.group7.shop.dto;

import com.group7.shop.entity.Product;

import java.time.Instant;

/** 历史列表条目。失败后恢复在售的商品也会出现，并标记 TRADE_FAILED。 */
public record ProductHistoryView(
        ProductView product,
        String historyType,
        Instant historyAt,
        boolean hadTradeFailure
) {
    public static ProductHistoryView from(Product product) {
        if (product.getClosedAt() != null) {
            String type = product.getCloseReason() == null
                    ? "OFF_SHELF"
                    : product.getCloseReason().name();
            return new ProductHistoryView(
                    ProductView.from(product),
                    type,
                    product.getClosedAt(),
                    product.getLastTradeFailedAt() != null
            );
        }
        return new ProductHistoryView(
                ProductView.from(product),
                "TRADE_FAILED",
                product.getLastTradeFailedAt(),
                true
        );
    }
}
