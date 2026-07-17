package com.enterprise.apimonetization.repository;

import com.enterprise.apimonetization.entity.ConsumerApp;
import com.enterprise.apimonetization.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsumerAppRepository extends JpaRepository<ConsumerApp, Long> {
    List<ConsumerApp> findByDeveloper(User developer);
    Page<ConsumerApp> findByDeveloper(User developer, Pageable pageable);
}
