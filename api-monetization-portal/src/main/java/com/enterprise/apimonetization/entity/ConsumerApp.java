package com.enterprise.apimonetization.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity
@Table(name = "consumer_apps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsumerApp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "developer_id", nullable = false)
    private User developer;

    // Safe flat fields for JSON — avoids lazy-loading the User proxy
    public Long getDeveloperId() {
        return developer != null ? developer.getId() : null;
    }

    public String getDeveloperUsername() {
        try {
            return developer != null ? developer.getUsername() : null;
        } catch (Exception e) {
            return null;
        }
    }

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
