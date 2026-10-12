package com.group7.shop.service;

import org.springframework.stereotype.Component;

/**
 * 商品状态改变时的跨域协作点。
 * 由意向域（负责人：华天遥）实现：商品手动下架后，剩余 WAITING 意向全部置为 FAILED（终态留痕）。
 * 「交易成功后其余意向置失败」由交易域（周琳皓）的 result 接口在标记该意向 TRADED 后调用
 * IntentService.failAllWaiting(productId) 完成。
 */
@Component
public class ProductLifecycleListener {

    private final IntentService intentService;

    public ProductLifecycleListener(IntentService intentService) {
        this.intentService = intentService;
    }

    public void onProductOffShelf(Long productId) {
        intentService.failAllWaiting(productId);
    }
}
