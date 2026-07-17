package com.enterprise.apimonetization.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@Entity
@Table(name = "apis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_path", nullable = false, unique = true, length = 100)
    private String basePath;

    @Column(nullable = false, length = 20)
    private String version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApiStatus status;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // Safe flat fields for JSON responses — avoid lazy-loading the full User proxy
    public Long getOwnerId() {
        return owner != null ? owner.getId() : null;
    }

    public String getOwnerUsername() {
        try {
            return owner != null ? owner.getUsername() : null;
        } catch (Exception e) {
            return null; // Hibernate proxy not initialized
        }
    }

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
