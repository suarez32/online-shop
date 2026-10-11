package com.group7.shop.service;

import com.group7.shop.entity.Intent;
import com.group7.shop.entity.IntentStatus;
import com.group7.shop.repository.IntentRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 商品状态改变时的跨域协作点。
 * 手动下架后将剩余排队意向标记失败，避免留下不可处理的队列记录。
 */
@Component
public class ProductLifecycleListener {
    private final IntentRepository intentRepository;

    public ProductLifecycleListener(IntentRepository intentRepository) {
        this.intentRepository = intentRepository;
    }

    public void onProductOffShelf(Long productId) {
        List<Intent> waiting = intentRepository
                .findByProductIdAndStatusOrderBySubmittedAtAscIdAsc(productId, IntentStatus.WAITING);
        waiting.forEach(Intent::markFailed);
        intentRepository.saveAll(waiting);
    }
}
