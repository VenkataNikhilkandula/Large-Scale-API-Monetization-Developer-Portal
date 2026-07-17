package com.enterprise.apimonetization.service;

import com.enterprise.apimonetization.dto.ApiKeyDetails;
import com.enterprise.apimonetization.entity.*;
import com.enterprise.apimonetization.repository.ApiInfoRepository;
import com.enterprise.apimonetization.repository.ConsumerAppRepository;
import com.enterprise.apimonetization.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionService {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private ConsumerAppRepository consumerAppRepository;

    @Autowired
    private ApiInfoRepository apiInfoRepository;

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String REDIS_KEY_PREFIX = "apikey:";

    @Transactional
    public Subscription subscribe(Long appId, Long apiId, SubscriptionTier tier) {
        ConsumerApp app = consumerAppRepository.findById(appId)
                .orElseThrow(() -> new IllegalArgumentException("App not found"));
        ApiInfo api = apiInfoRepository.findById(apiId)
                .orElseThrow(() -> new IllegalArgumentException("API not found"));

        if (api.getStatus() != ApiStatus.PUBLISHED) {
            throw new IllegalStateException("Cannot subscribe to an API that is not PUBLISHED");
        }

        Optional<Subscription> existing = subscriptionRepository
                .findByApp_IdAndApi_IdAndStatus(appId, apiId, SubscriptionStatus.ACTIVE);
        if (existing.isPresent()) {
            throw new IllegalStateException("App is already subscribed to this API");
        }

        Subscription subscription = Subscription.builder()
                .app(app)
                .api(api)
                .tier(tier)
                .status(SubscriptionStatus.ACTIVE)
                .startsAt(LocalDateTime.now())
                .build();

        Subscription saved = subscriptionRepository.save(subscription);
        refreshRedisKeysForApp(appId, tier.name(), tier.getRateLimitPerSecond());
        return saved;
    }

    @Transactional
    public Subscription upgradeDowngrade(Long subscriptionId, SubscriptionTier newTier) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));

        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new IllegalStateException("Subscription is not ACTIVE");
        }

        subscription.setTier(newTier);
        Subscription saved = subscriptionRepository.save(subscription);
        refreshRedisKeysForApp(subscription.getApp().getId(), newTier.name(), newTier.getRateLimitPerSecond());
        return saved;
    }

    @Transactional
    public Subscription cancelSubscription(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setEndsAt(LocalDateTime.now());
        Subscription saved = subscriptionRepository.save(subscription);
        refreshRedisKeysForApp(subscription.getApp().getId(), "FREE", 2);
        return saved;
    }

    public List<Subscription> getSubscriptionsByApp(Long appId) {
        return subscriptionRepository.findByApp_IdAndStatus(appId, SubscriptionStatus.ACTIVE);
    }

    /**
     * Refreshes cached API key details in Redis when the subscription tier changes.
     * Silently skips if Redis is not available.
     */
    private void refreshRedisKeysForApp(Long appId, String tier, int rateLimit) {
        List<ApiKey> keys = apiKeyService.getKeysByApp(appId);
        for (ApiKey key : keys) {
            if (key.isActive()) {
                try {
                    String redisKey = REDIS_KEY_PREFIX + key.getKeyValue();
                    ApiKeyDetails details = ApiKeyDetails.builder()
                            .keyValue(key.getKeyValue())
                            .appId(appId)
                            .developerId(key.getApp().getDeveloper().getId())
                            .tier(tier)
                            .rateLimitPerSecond(rateLimit)
                            .active(true)
                            .build();
                    redisTemplate.opsForValue().set(redisKey, details);
                } catch (Exception e) {
                    logger.warn("Redis unavailable, could not refresh key cache for appId={}: {}", appId, e.getMessage());
                }
            }
        }
    }
}
