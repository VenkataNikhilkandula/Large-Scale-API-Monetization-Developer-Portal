package com.enterprise.apimonetization.service;

import com.enterprise.apimonetization.dto.AnalyticsDashboardResponse;
import com.enterprise.apimonetization.entity.ApiUsage;
import com.enterprise.apimonetization.repository.ApiInfoRepository;
import com.enterprise.apimonetization.repository.ApiUsageRepository;
import com.enterprise.apimonetization.repository.SubscriptionRepository;
import com.enterprise.apimonetization.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    @Autowired
    private ApiInfoRepository apiInfoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private ApiUsageRepository apiUsageRepository;

    public AnalyticsDashboardResponse getDashboardStats() {
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        long totalApis = apiInfoRepository.count();
        long totalDevelopers = userRepository.count();
        long totalSubscriptions = subscriptionRepository.count();

        List<ApiUsage> currentUsages = apiUsageRepository.findAll(); // Simple aggregation logic
        long totalRequests = 0;
        Map<String, Long> apiUsageMap = new HashMap<>();

        for (ApiUsage usage : currentUsages) {
            if (currentMonth.equals(usage.getBillingMonth())) {
                totalRequests += usage.getRequestCount();
                apiUsageMap.put(usage.getApi().getName(), 
                        apiUsageMap.getOrDefault(usage.getApi().getName(), 0L) + usage.getRequestCount());
            }
        }

        return AnalyticsDashboardResponse.builder()
                .totalApis(totalApis)
                .totalDevelopers(totalDevelopers)
                .totalSubscriptions(totalSubscriptions)
                .totalRequestsThisMonth(totalRequests)
                .requestsPerApi(apiUsageMap)
                .build();
    }
}
