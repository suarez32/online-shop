package com.group7.shop.repository;

import com.group7.shop.entity.Intent;
import com.group7.shop.entity.IntentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 意向数据访问接口（意向域负责人：华天遥）。
 * 方法为账号域（李宇轩 overview）与意向域（华天遥）的并集，融合自双方分支。
 */
public interface IntentRepository extends JpaRepository<Intent, Long> {

    /** 按提交时间正序返回某商品的全部意向，供后台总览排队展示（账号域使用）。 */
    List<Intent> findByProductIdOrderBySubmittedAtAsc(Long productId);

    /** 凭口令码哈希定位意向（买家查询/修改/撤销的唯一入口）。 */
    Optional<Intent> findByCodeHash(String codeHash);

    boolean existsByCodeHash(String codeHash);

    /** 某商品的指定状态意向，按提交时间先到先得（id 兜底同毫秒排序）。 */
    List<Intent> findByProductIdAndStatusOrderBySubmittedAtAscIdAsc(Long productId, IntentStatus status);

    /** 工作台意向列表：排队中 + 交易中的活跃意向，同样按提交时间正序。 */
    List<Intent> findByProductIdAndStatusInOrderBySubmittedAtAscIdAsc(Long productId, List<IntentStatus> statuses);

    /** 商品下架/交易成功时批量把剩余排队意向置为失败。 */
    List<Intent> findByProductIdAndStatus(Long productId, IntentStatus status);
}
