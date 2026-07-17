package com.enterprise.apimonetization.service;

import com.enterprise.apimonetization.config.KafkaConfig;
import com.enterprise.apimonetization.dto.ApiUsageEvent;
import com.enterprise.apimonetization.entity.ApiInfo;
import com.enterprise.apimonetization.entity.ApiUsage;
import com.enterprise.apimonetization.entity.ConsumerApp;
import com.enterprise.apimonetization.entity.Subscription;
import com.enterprise.apimonetization.entity.SubscriptionStatus;
import com.enterprise.apimonetization.repository.ApiInfoRepository;
import com.enterprise.apimonetization.repository.ApiUsageRepository;
import com.enterprise.apimonetization.repository.ConsumerAppRepository;
import com.enterprise.apimonetization.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class UsageTrackingService {

    private static final Logger logger = LoggerFactory.getLogger(UsageTrackingService.class);

    @Autowired
    private KafkaTemplate<String, ApiUsageEvent> kafkaTemplate;

    @Autowired
    private ApiUsageRepository apiUsageRepository;

    @Autowired
    private ConsumerAppRepository consumerAppRepository;

    @Autowired
    private ApiInfoRepository apiInfoRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    /**
     * Publishes a usage event to Kafka asynchronously.
     * If Kafka is unavailable, logs the usage directly to the database instead.
     */
    public void logUsage(Long appId, Long apiId, String basePath, long responseTimeMs, int statusCode) {
        ApiUsageEvent event = ApiUsageEvent.builder()
                .appId(appId)
                .apiId(apiId)
                .basePath(basePath)
                .responseTimeMs(responseTimeMs)
                .statusCode(statusCode)
                .timestamp(LocalDateTime.now().toString())
                .build();

        try {
            logger.info("Publishing API usage event to Kafka: appId={}, apiId={}", appId, apiId);
            kafkaTemplate.send(KafkaConfig.USAGE_TOPIC, event);
        } catch (Exception e) {
            // Kafka unavailable — fall back to direct DB write
            logger.warn("Kafka unavailable, falling back to direct DB write for usage tracking: {}", e.getMessage());
            try {
                persistUsageDirectly(event);
            } catch (Exception dbEx) {
                logger.error("Failed to persist usage directly to DB: {}", dbEx.getMessage());
            }
        }
    }

    /**
     * Kafka consumer — processes usage events from the Kafka topic.
     * If Kafka is not running this method simply won't be invoked.
     */
    @KafkaListener(topics = KafkaConfig.USAGE_TOPIC, groupId = "api-portal-group",
                   autoStartup = "${kafka.consumer.enabled:true}")
    @Transactional
    public void consumeUsageEvent(ApiUsageEvent event) {
        logger.info("Received API usage event from Kafka: appId={}, apiId={}", event.getAppId(), event.getApiId());
        persistUsageDirectly(event);
    }

    /**
     * Core logic: increment DB request count and update Redis quota counter.
     */
    @Transactional
    public void persistUsageDirectly(ApiUsageEvent event) {
        String billingMonth = LocalDateTime.now().format(MONTH_FORMATTER);

        ConsumerApp app = consumerAppRepository.findById(event.getAppId())
                .orElseThrow(() -> new IllegalArgumentException("App not found: " + event.getAppId()));

        ApiInfo api = apiInfoRepository.findById(event.getApiId())
                .orElseThrow(() -> new IllegalArgumentException("API not found: " + event.getApiId()));

        ApiUsage usage = apiUsageRepository
                .findByApp_IdAndApi_IdAndBillingMonth(event.getAppId(), event.getApiId(), billingMonth)
                .orElseGet(() -> ApiUsage.builder()
                        .app(app)
                        .api(api)
                        .billingMonth(billingMonth)
                        .requestCount(0L)
                        .overageCount(0L)
                        .build());

        usage.setRequestCount(usage.getRequestCount() + 1);

        // Overage check
        Optional<Subscription> subOpt = subscriptionRepository
                .findByApp_IdAndApi_IdAndStatus(event.getAppId(), event.getApiId(), SubscriptionStatus.ACTIVE);
        long quotaLimit = 1000L;
        if (subOpt.isPresent()) {
            quotaLimit = subOpt.get().getTier().getMonthlyQuota();
        }
        if (usage.getRequestCount() > quotaLimit) {
            usage.setOverageCount(usage.getRequestCount() - quotaLimit);
        }

        apiUsageRepository.save(usage);

        // Update Redis quota counter — if Redis is unavailable, just skip
        try {
            String redisKey = "quota_used:" + event.getAppId() + ":" + event.getApiId() + ":" + billingMonth;
            redisTemplate.opsForValue().increment(redisKey);
        } catch (Exception e) {
            logger.warn("Redis unavailable, skipping quota counter update: {}", e.getMessage());
        }
    }

    /**
     * Checks if the monthly quota is exceeded.
     * Uses Redis for speed; falls back to MySQL if Redis is down.
     */
    public boolean isQuotaExceeded(Long appId, Long apiId) {
        String billingMonth = LocalDateTime.now().format(MONTH_FORMATTER);
        String redisKey = "quota_used:" + appId + ":" + apiId + ":" + billingMonth;

        long used = 0;

        // Try Redis first
        try {
            Object cacheVal = redisTemplate.opsForValue().get(redisKey);
            if (cacheVal != null) {
                used = Long.parseLong(cacheVal.toString());
            } else {
                // Cache miss — load from DB
                Optional<ApiUsage> usageOpt = apiUsageRepository
                        .findByApp_IdAndApi_IdAndBillingMonth(appId, apiId, billingMonth);
                if (usageOpt.isPresent()) {
                    used = usageOpt.get().getRequestCount();
                    redisTemplate.opsForValue().set(redisKey, used);
                }
            }
        } catch (Exception e) {
            // Redis unavailable — fallback to DB only
            logger.warn("Redis unavailable for quota check, querying DB: {}", e.getMessage());
            Optional<ApiUsage> usageOpt = apiUsageRepository
                    .findByApp_IdAndApi_IdAndBillingMonth(appId, apiId, billingMonth);
            if (usageOpt.isPresent()) {
                used = usageOpt.get().getRequestCount();
            }
        }

        Optional<Subscription> subOpt = subscriptionRepository
                .findByApp_IdAndApi_IdAndStatus(appId, apiId, SubscriptionStatus.ACTIVE);
        long limit = 1000L;
        if (subOpt.isPresent()) {
            limit = subOpt.get().getTier().getMonthlyQuota();
        }

        return used >= limit;
    }
}
