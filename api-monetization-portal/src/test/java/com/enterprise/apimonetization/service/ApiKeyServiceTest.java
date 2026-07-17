package com.enterprise.apimonetization.service;

import com.enterprise.apimonetization.dto.ApiKeyDetails;
import com.enterprise.apimonetization.entity.ApiKey;
import com.enterprise.apimonetization.entity.ConsumerApp;
import com.enterprise.apimonetization.entity.User;
import com.enterprise.apimonetization.repository.ApiKeyRepository;
import com.enterprise.apimonetization.repository.ConsumerAppRepository;
import com.enterprise.apimonetization.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ApiKeyServiceTest {

    @Mock
    private ApiKeyRepository apiKeyRepository;

    @Mock
    private ConsumerAppRepository consumerAppRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private ApiKeyService apiKeyService;

    @BeforeEach
    void setUp() {
        // Mock Redis Template value operations
        Mockito.lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    public void testValidateKey_CacheHit() {
        ApiKeyDetails mockDetails = ApiKeyDetails.builder()
                .keyValue("test_key")
                .appId(1L)
                .developerId(2L)
                .tier("PREMIUM")
                .rateLimitPerSecond(50)
                .active(true)
                .build();

        when(valueOperations.get("apikey:test_key")).thenReturn(mockDetails);

        ApiKeyDetails details = apiKeyService.validateKey("test_key");

        assertNotNull(details);
        assertEquals("PREMIUM", details.getTier());
        assertEquals(50, details.getRateLimitPerSecond());
        assertTrue(details.isActive());
    }

    @Test
    public void testValidateKey_CacheMiss_Success() {
        User developer = User.builder().id(2L).username("dev").email("dev@ex.com").build();
        ConsumerApp app = ConsumerApp.builder().id(1L).name("App").developer(developer).build();
        ApiKey apiKey = ApiKey.builder().id(10L).keyValue("test_key").app(app).active(true).build();

        when(valueOperations.get("apikey:test_key")).thenReturn(null);
        when(apiKeyRepository.findByKeyValueAndActiveTrue("test_key")).thenReturn(Optional.of(apiKey));
        when(subscriptionRepository.findByApp_IdAndStatus(eq(1L), any())).thenReturn(new ArrayList<>());

        ApiKeyDetails details = apiKeyService.validateKey("test_key");

        assertNotNull(details);
        assertEquals("FREE", details.getTier()); // Defaults to free when no subscription exists
        assertEquals(2, details.getRateLimitPerSecond());
    }

    @Test
    public void testValidateKey_InvalidKey_ThrowsException() {
        when(valueOperations.get("apikey:invalid_key")).thenReturn(null);
        when(apiKeyRepository.findByKeyValueAndActiveTrue("invalid_key")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            apiKeyService.validateKey("invalid_key");
        });
    }
}
