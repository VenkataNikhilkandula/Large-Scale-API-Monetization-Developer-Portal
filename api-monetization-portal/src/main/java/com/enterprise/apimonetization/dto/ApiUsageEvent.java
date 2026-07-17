package com.enterprise.apimonetization.dto;

import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ApiUsageEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long appId;
    private Long apiId;
    private String basePath;
    private Long responseTimeMs;
    private int statusCode;
    private String timestamp;
}
