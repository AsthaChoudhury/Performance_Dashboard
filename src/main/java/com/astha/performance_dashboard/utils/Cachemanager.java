package com.astha.performance_dashboard.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class Cachemanager {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    @Value("${cache.default-ttl:300}")
    private long defaultTtl;

    @Value("${cache.analytics-ttl:600}")
    private long analyticsTtl;

    @Value("${cache.asset-stats-ttl:60}")
    private long assetStatsTtl;

    /**
     * Cache-aside pattern: Try to get from cache, if miss, compute and store
     */
    public <T> Mono<T> getOrCompute(String key, Class<T> clazz, Supplier<Mono<T>> supplier) {
        return getOrCompute(key, clazz, supplier, defaultTtl);
    }

    public <T> Mono<T> getOrCompute(String key, Class<T> clazz, Supplier<Mono<T>> supplier, long ttlSeconds) {
        return redisTemplate.opsForValue()
            .get(key)
            .cast(clazz)
            .doOnNext(cached -> log.debug("Cache HIT for key: {}", key))
            .switchIfEmpty(
                supplier.get()
                    .flatMap(computed -> 
                        set(key, computed, ttlSeconds)
                            .thenReturn(computed)
                    )
                    .doOnNext(computed -> log.debug("Cache MISS for key: {}, computed and stored", key))
            )
            .doOnError(error -> log.error("Cache operation failed for key: {}", key, error));
    }

    /**
     * Set value in cache with TTL
     */
    public Mono<Boolean> set(String key, Object value, long ttlSeconds) {
        return redisTemplate.opsForValue()
            .set(key, value, Duration.ofSeconds(ttlSeconds))
            .doOnSuccess(result -> log.debug("Cached key: {} with TTL: {}s", key, ttlSeconds))
            .onErrorReturn(false);
    }

    /**
     * Get value from cache
     */
    public <T> Mono<T> get(String key, Class<T> clazz) {
        return redisTemplate.opsForValue()
            .get(key)
            .cast(clazz)
            .doOnNext(value -> log.debug("Retrieved from cache: {}", key))
            .onErrorResume(error -> {
                log.error("Failed to get from cache: {}", key, error);
                return Mono.empty();
            });
    }

    /**
     * Invalidate cache entry
     */
    public Mono<Boolean> invalidate(String key) {
        return redisTemplate.delete(key)
            .map(count -> count > 0)
            .doOnNext(deleted -> {
                if (deleted) {
                    log.debug("Invalidated cache key: {}", key);
                }
            })
            .onErrorReturn(false);
    }

    /**
     * Invalidate multiple cache entries matching pattern
     */
    public Mono<Long> invalidatePattern(String pattern) {
        return redisTemplate.keys(pattern)
            .collectList()
            .flatMap(keys -> {
                if (keys.isEmpty()) {
                    return Mono.just(0L);
                }
                return redisTemplate.delete(keys.toArray(new String[0]))
                    .doOnNext(count -> log.debug("Invalidated {} keys matching pattern: {}", count, pattern));
            })
            .onErrorReturn(0L);
    }

    /**
     * Check if key exists in cache
     */
    public Mono<Boolean> exists(String key) {
        return redisTemplate.hasKey(key)
            .onErrorReturn(false);
    }

    /**
     * Get cache key for asset stats
     */
    public String getAssetStatsKey(String assetId) {
        return "asset:stats:" + assetId;
    }

    /**
     * Get cache key for analytics
     */
    public String getAnalyticsKey(String type, String... params) {
        return "analytics:" + type + ":" + String.join(":", params);
    }

    /**
     * Get cache key for performance trends
     */
    public String getTrendKey(String assetId, String period) {
        return "trend:" + assetId + ":" + period;
    }

    /**
     * Get TTL for analytics cache
     */
    public long getAnalyticsTtl() {
        return analyticsTtl;
    }

    /**
     * Get TTL for asset stats cache
     */
    public long getAssetStatsTtl() {
        return assetStatsTtl;
    }
}
