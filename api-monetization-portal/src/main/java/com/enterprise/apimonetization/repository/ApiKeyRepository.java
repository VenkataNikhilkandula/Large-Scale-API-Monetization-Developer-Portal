package com.enterprise.apimonetization.repository;

import com.enterprise.apimonetization.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    Optional<ApiKey> findByKeyValueAndActiveTrue(String keyValue);
    // 'app' is a @ManyToOne field — use app_Id to traverse to the FK column
    List<ApiKey> findByApp_Id(Long appId);
}

