package com.enterprise.apimonetization.repository;

import com.enterprise.apimonetization.entity.ApiInfo;
import com.enterprise.apimonetization.entity.ApiStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiInfoRepository extends JpaRepository<ApiInfo, Long> {
    Optional<ApiInfo> findByBasePath(String basePath);

    Page<ApiInfo> findByStatus(ApiStatus status, Pageable pageable);

    @Query("SELECT a FROM ApiInfo a WHERE a.status = :status AND " +
           "(LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<ApiInfo> searchCatalog(@Param("status") ApiStatus status, @Param("query") String query, Pageable pageable);
}
