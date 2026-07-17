package com.enterprise.apimonetization.repository;

import com.enterprise.apimonetization.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    // 'app' is a @ManyToOne field — use _Id to traverse to the FK
    List<Invoice> findByApp_Id(Long appId);
    Optional<Invoice> findByApp_IdAndBillingMonth(Long appId, String billingMonth);
}
