package com.group7.shop.service;

import org.springframework.stereotype.Component;

/**
 * 商品状态改变时的跨域协作点。
 * 集成时应由意向/交易模块实现队列意向清理逻辑。
 */
@Component
public class ProductLifecycleListener {
    public void onProductOffShelf(Long productId) {
        // TODO 集成华天遥/周琳皓的 IntentService：
        // 商品手动下架后，将该商品剩余 WAITING 意向标记为 FAILED。
        // 当前实现不假装已经更新了 Intent 表。
    }
}
