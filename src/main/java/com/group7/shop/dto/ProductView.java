package com.group7.shop.dto;

import com.group7.shop.entity.Product;
import com.group7.shop.entity.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;

/** API 展示 DTO：对外字段名使用 imageUrl，实体字段仍为 image。 */
public record ProductView(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String imageUrl,
        ProductStatus status,
        Instant createdAt,
        Instant closedAt
) {
    public static ProductView from(Product product) {
        return new ProductView(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getImage(),
                product.getStatus(),
                product.getCreatedAt(),
                product.getClosedAt()
        );
    }
}
