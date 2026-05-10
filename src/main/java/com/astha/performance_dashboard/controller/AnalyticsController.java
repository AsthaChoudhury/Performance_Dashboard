package com.astha.performance_dashboard.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.astha.performance_dashboard.dto.AssetStatsResponseDTO;
import com.astha.performance_dashboard.dto.CachePerformanceResponseDTO;
import com.astha.performance_dashboard.dto.ComparativeAnalyticsResponseDTO;
import com.astha.performance_dashboard.dto.PerformanceTrendResponseDTO;
import com.astha.performance_dashboard.service.AnalyticsService;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/assets/{assetId}/stats")
    public Mono<ResponseEntity<AssetStatsResponseDTO>> getAssetStats(
            @PathVariable String assetId) {
        return analyticsService.getAssetStats(assetId)
            .map(ResponseEntity::ok)
            .doOnNext(response -> log.debug("Retrieved stats for asset: {}", assetId));
    }

    @GetMapping("/assets/{assetId}/trends")
    public Mono<ResponseEntity<PerformanceTrendResponseDTO>> getPerformanceTrend(
            @PathVariable String assetId,
            @RequestParam(defaultValue = "24h") String period) {
        return analyticsService.getPerformanceTrend(assetId, period)
            .map(ResponseEntity::ok);
    }

    @PostMapping("/compare")
    public Mono<ResponseEntity<ComparativeAnalyticsResponseDTO>> compareAssets(
            @RequestBody List<String> assetIds) {
        return analyticsService.compareAssets(assetIds)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/assets/{assetId}/health-score")
public Mono<ResponseEntity<Map<String, Double>>> calculateHealthScore(@PathVariable String assetId) {
    return analyticsService.calculateHealthScore(assetId)
        .map(score -> {
            Map<String, Double> response = Map.of("healthScore", score);
            return ResponseEntity.ok(response);
        });
}

    /**
     * Demonstrates cache performance - shows cached vs non-cached response times
     */
    @GetMapping("/cache-demo/{assetId}")
    public Mono<ResponseEntity<CachePerformanceResponseDTO>> getCachePerformanceDemo(
            @PathVariable String assetId) {
        return analyticsService.getCachePerformanceDemo(assetId)
            .map(ResponseEntity::ok)
            .doOnNext(response -> {
                var body = response.getBody();
                if (body != null) {
                    log.info("Cache performance - Cached: {}ms, Non-cached: {}ms, Speedup: {}x",
                        body.getCachedResponseTime(),
                        body.getNonCachedResponseTime(),
                        String.format("%.2f", body.getSpeedupFactor()));
                }
            });
    }
}
