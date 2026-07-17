package com.enterprise.apimonetization.service;

import com.enterprise.apimonetization.dto.ApiKeyDetails;
import com.enterprise.apimonetization.entity.ApiKey;
import com.enterprise.apimonetization.entity.ConsumerApp;
import com.enterprise.apimonetization.entity.Subscription;
import com.enterprise.apimonetization.entity.SubscriptionStatus;
import com.enterprise.apimonetization.repository.ApiKeyRepository;
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
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class ApiKeyService {

    private static final Logger logger = LoggerFactory.getLogger(ApiKeyService.class);

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private ConsumerAppRepository consumerAppRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String REDIS_KEY_PREFIX = "apikey:";

    @Transactional
    public ApiKey generateKey(Long appId) {
        ConsumerApp app = consumerAppRepository.findById(appId)
                .orElseThrow(() -> new IllegalArgumentException("App not found"));

        String rawKey = "key_" + UUID.randomUUID().toString().replace("-", "");
        ApiKey apiKey = ApiKey.builder()
                .keyValue(rawKey)
                .app(app)
                .active(true)
                .expiresAt(LocalDateTime.now().plusYears(1))
                .build();

        ApiKey saved = apiKeyRepository.save(apiKey);
        tryCache(saved);
        return saved;
    }

    @Transactional
    public ApiKey regenerateKey(Long keyId) {
        ApiKey apiKey = apiKeyRepository.findById(keyId)
                .orElseThrow(() -> new IllegalArgumentException("API Key not found"));

        // Revoke old key
        apiKey.setActive(false);
        apiKeyRepository.save(apiKey);
        tryEvict(apiKey.getKeyValue());

        // Generate new key
        return generateKey(apiKey.getApp().getId());
    }

    public List<ApiKey> getKeysByApp(Long appId) {
        return apiKeyRepository.findByApp_Id(appId);
    }

    /**
     * Validates an API key — Redis-first, falls back to MySQL if Redis is unavailable.
     */
    public ApiKeyDetails validateKey(String keyValue) {
        // 1. Try Redis cache first
        try {
            String redisKey = REDIS_KEY_PREFIX + keyValue;
            ApiKeyDetails cached = (ApiKeyDetails) redisTemplate.opsForValue().get(redisKey);
            if (cached != null) {
                return cached;
            }
        } catch (Exception e) {
            logger.warn("Redis unavailable for key lookup, falling back to DB: {}", e.getMessage());
        }

        // 2. Cache miss or Redis down — query MySQL
        ApiKey apiKey = apiKeyRepository.findByKeyValueAndActiveTrue(keyValue)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or inactive API Key"));

        List<Subscription> subs = subscriptionRepository
                .findByApp_IdAndStatus(apiKey.getApp().getId(), SubscriptionStatus.ACTIVE);

        String plan = "FREE";
        int rateLimit = 2;
        if (!subs.isEmpty()) {
            Subscription mainSub = subs.get(0);
            plan = mainSub.getTier().name();
            rateLimit = mainSub.getTier().getRateLimitPerSecond();
        }

        ApiKeyDetails details = ApiKeyDetails.builder()
                .keyValue(apiKey.getKeyValue())
                .appId(apiKey.getApp().getId())
                .developerId(apiKey.getApp().getDeveloper().getId())
                .tier(plan)
                .rateLimitPerSecond(rateLimit)
                .active(apiKey.isActive())
                .build();

        // 3. Try to cache result for future lookups
        try {
            redisTemplate.opsForValue().set(REDIS_KEY_PREFIX + keyValue, details, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            logger.warn("Redis unavailable for caching key details: {}", e.getMessage());
        }

        return details;
    }

    private void tryCache(ApiKey apiKey) {
        try {
            ApiKeyDetails details = ApiKeyDetails.builder()
                    .keyValue(apiKey.getKeyValue())
                    .appId(apiKey.getApp().getId())
                    .developerId(apiKey.getApp().getDeveloper().getId())
                    .tier("FREE")
                    .rateLimitPerSecond(2)
                    .active(apiKey.isActive())
                    .build();
            redisTemplate.opsForValue().set(REDIS_KEY_PREFIX + apiKey.getKeyValue(), details, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            logger.warn("Redis unavailable, API key not cached: {}", e.getMessage());
        }
    }

    private void tryEvict(String keyValue) {
        try {
            redisTemplate.delete(REDIS_KEY_PREFIX + keyValue);
        } catch (Exception e) {
            logger.warn("Redis unavailable, could not evict key: {}", e.getMessage());
        }
    }
}
