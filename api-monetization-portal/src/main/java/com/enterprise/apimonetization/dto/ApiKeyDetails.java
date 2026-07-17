package com.enterprise.apimonetization.dto;

import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiKeyDetails implements Serializable {
    private static final long serialVersionUID = 1L;

    private String keyValue;
    private Long appId;
    private Long developerId;
    private String tier;
    private int rateLimitPerSecond;
    private boolean active;
}
