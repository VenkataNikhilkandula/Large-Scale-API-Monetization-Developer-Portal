package com.enterprise.apimonetization.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "app_id", nullable = false)
    private ConsumerApp app;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "api_id", nullable = false)
    private ApiInfo api;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SubscriptionTier tier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SubscriptionStatus status;

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "ends_at")
    private LocalDateTime endsAt;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // Safe flat fields for JSON serialization
    public Long getAppId() {
        return app != null ? app.getId() : null;
    }

    public String getAppName() {
        try { return app != null ? app.getName() : null; } catch (Exception e) { return null; }
    }

    public Long getApiId() {
        return api != null ? api.getId() : null;
    }

    public String getApiName() {
        try { return api != null ? api.getName() : null; } catch (Exception e) { return null; }
    }
}
