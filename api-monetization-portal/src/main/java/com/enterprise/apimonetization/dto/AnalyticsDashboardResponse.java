package com.enterprise.apimonetization.dto;

import lombok.*;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsDashboardResponse {
    private long totalApis;
    private long totalDevelopers;
    private long totalSubscriptions;
    private long totalRequestsThisMonth;
    private Map<String, Long> requestsPerApi;
}
