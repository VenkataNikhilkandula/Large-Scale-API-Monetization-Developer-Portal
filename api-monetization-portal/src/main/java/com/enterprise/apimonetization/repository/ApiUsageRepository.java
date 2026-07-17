package com.enterprise.apimonetization.repository;

import com.enterprise.apimonetization.entity.ApiUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiUsageRepository extends JpaRepository<ApiUsage, Long> {
    // 'app' and 'api' are @ManyToOne fields — use _Id to traverse to the FK
    Optional<ApiUsage> findByApp_IdAndApi_IdAndBillingMonth(Long appId, Long apiId, String billingMonth);
    List<ApiUsage> findByApp_IdAndBillingMonth(Long appId, String billingMonth);
}
