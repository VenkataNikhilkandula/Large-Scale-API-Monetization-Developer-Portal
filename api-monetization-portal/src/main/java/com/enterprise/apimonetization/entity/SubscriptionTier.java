package com.enterprise.apimonetization.entity;

import lombok.Getter;
import java.math.BigDecimal;

@Getter
public enum SubscriptionTier {
    FREE(BigDecimal.ZERO, 1000L, BigDecimal.ZERO, 2),
    BASIC(new BigDecimal("29.00"), 10000L, new BigDecimal("0.01"), 10),
    PREMIUM(new BigDecimal("99.00"), 100000L, new BigDecimal("0.005"), 50),
    ENTERPRISE(new BigDecimal("499.00"), 1000000L, new BigDecimal("0.002"), 200);

    private final BigDecimal basePrice;
    private final Long monthlyQuota;
    private final BigDecimal overageRate;
    private final int rateLimitPerSecond;

    SubscriptionTier(BigDecimal basePrice, Long monthlyQuota, BigDecimal overageRate, int rateLimitPerSecond) {
        this.basePrice = basePrice;
        this.monthlyQuota = monthlyQuota;
        this.overageRate = overageRate;
        this.rateLimitPerSecond = rateLimitPerSecond;
    }
}
