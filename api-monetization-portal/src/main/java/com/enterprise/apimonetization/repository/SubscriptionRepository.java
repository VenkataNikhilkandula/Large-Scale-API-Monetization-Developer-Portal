package com.enterprise.apimonetization.repository;

import com.enterprise.apimonetization.entity.Subscription;
import com.enterprise.apimonetization.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    // 'app' and 'api' are @ManyToOne fields — use _Id suffix to traverse the FK
    Optional<Subscription> findByApp_IdAndApi_IdAndStatus(Long appId, Long apiId, SubscriptionStatus status);
    List<Subscription> findByApp_IdAndStatus(Long appId, SubscriptionStatus status);
    List<Subscription> findByStatus(SubscriptionStatus status);
}

