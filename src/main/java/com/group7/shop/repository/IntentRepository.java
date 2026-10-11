package com.group7.shop.repository;

import com.group7.shop.entity.Intent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IntentRepository extends JpaRepository<Intent, Long> {

    /** 按提交时间正序返回某商品的全部意向，供后台总览排队展示。 */
    List<Intent> findByProductIdOrderBySubmittedAtAsc(Long productId);
}
