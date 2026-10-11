package com.group7.shop.repository;

import com.group7.shop.entity.Intent;
import com.group7.shop.entity.IntentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/** 交易域所需的意向数据访问接口，意向域可以直接复用。 */
public interface IntentRepository extends JpaRepository<Intent, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Intent i where i.id = :id")
    Optional<Intent> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Intent i where i.productId = :productId and i.status = com.group7.shop.entity.IntentStatus.IN_TRADE")
    Optional<Intent> findActiveTradeForUpdate(@Param("productId") Long productId);

    List<Intent> findByProductIdAndStatusInOrderBySubmittedAtAscIdAsc(
            Long productId, List<IntentStatus> statuses);

    List<Intent> findByProductIdAndStatusOrderBySubmittedAtAscIdAsc(
            Long productId, IntentStatus status);
}
