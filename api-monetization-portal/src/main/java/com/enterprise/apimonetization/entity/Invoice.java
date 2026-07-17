package com.enterprise.apimonetization.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "app_id", nullable = false)
    private ConsumerApp app;

    // Safe flat fields for JSON serialization
    public Long getAppId() {
        return app != null ? app.getId() : null;
    }

    public String getAppName() {
        try { return app != null ? app.getName() : null; } catch (Exception e) { return null; }
    }

    @Column(name = "billing_month", nullable = false, length = 7)
    private String billingMonth; // YYYY-MM

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InvoiceStatus status;

    @Column(name = "generated_at", updatable = false)
    @Builder.Default
    private LocalDateTime generatedAt = LocalDateTime.now();

    @Column(name = "paid_at")
    private LocalDateTime paidAt;
}
