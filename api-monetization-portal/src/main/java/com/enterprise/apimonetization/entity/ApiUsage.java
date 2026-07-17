package com.enterprise.apimonetization.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "api_usages",
    uniqueConstraints = @UniqueConstraint(columnNames = {"app_id", "api_id", "billing_month"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "app_id", nullable = false)
    private ConsumerApp app;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "api_id", nullable = false)
    private ApiInfo api;

    @Column(name = "billing_month", nullable = false, length = 7)
    private String billingMonth; // YYYY-MM

    @Column(name = "request_count")
    @Builder.Default
    private Long requestCount = 0L;

    @Column(name = "overage_count")
    @Builder.Default
    private Long overageCount = 0L;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
