package com.group7.shop.dto;

/**
 * GET /api/intents/{code} 的出参：{ intent, product }。
 */
public record IntentQueryResult(BuyerIntentView intent, ProductView product) {
}
