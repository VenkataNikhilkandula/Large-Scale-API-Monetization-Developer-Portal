package com.enterprise.apimonetization.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity
@Table(name = "api_keys")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "key_value", nullable = false, unique = true)
    private String keyValue;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "app_id", nullable = false)
    private ConsumerApp app;

    // Safe flat fields for JSON
    public Long getAppId() {
        return app != null ? app.getId() : null;
    }

    public String getAppName() {
        try { return app != null ? app.getName() : null; } catch (Exception e) { return null; }
    }

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}
