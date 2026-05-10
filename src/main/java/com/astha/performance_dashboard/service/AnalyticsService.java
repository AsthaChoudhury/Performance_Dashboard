package com.astha.performance_dashboard.service;

import com.astha.performance_dashboard.dto.*;
import com.astha.performance_dashboard.model.PerformanceMetric;
import com.astha.performance_dashboard.repository.AssetRepository;
import com.astha.performance_dashboard.repository.PerformanceMetricRepository;
import com.astha.performance_dashboard.utils.Cachemanager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final PerformanceMetricRepository metricRepository;
    private final AssetRepository assetRepository;
    private final ReactiveMongoTemplate mongoTemplate;
    private final Cachemanager cacheManager;

    /**
     * Get comprehensive stats for an asset with caching
     */
    public Mono<AssetStatsResponseDTO> getAssetStats(String assetId) {
        String cacheKey = cacheManager.getAssetStatsKey(assetId);
        
        return cacheManager.getOrCompute(
            cacheKey,
            AssetStatsResponseDTO.class,
            () -> computeAssetStats(assetId),
            cacheManager.getAssetStatsTtl()
        );
    }

    private Mono<AssetStatsResponseDTO> computeAssetStats(String assetId) {
        return Mono.zip(
            assetRepository.findById(assetId),
            metricRepository.findByAssetIdOrderByTimestampDesc(assetId).collectList(),
            metricRepository.countByAssetId(assetId),
            metricRepository.countFailuresByAssetId(assetId),
            metricRepository.findFirstByAssetIdOrderByTimestampDesc(assetId)
        ).map(tuple -> {
            var asset = tuple.getT1();
            var metrics = tuple.getT2();
            var totalCount = tuple.getT3();
            var failureCount = tuple.getT4();
            var lastMetric = tuple.getT5();

            if (metrics.isEmpty()) {
                return AssetStatsResponseDTO.builder()
                    .assetId(assetId)
                    .assetName(asset.getAssetName())
                    .status(asset.getStatus())
                    .build();
            }

            double avgTemp = metrics.stream()
                .filter(m -> m.getTemperature() != null)
                .mapToDouble(PerformanceMetric::getTemperature)
                .average()
                .orElse(0.0);

            double avgVibration = metrics.stream()
                .filter(m -> m.getVibration() != null)
                .mapToDouble(PerformanceMetric::getVibration)
                .average()
                .orElse(0.0);

            double avgEfficiency = metrics.stream()
                .filter(m -> m.getEfficiency() != null)
                .mapToDouble(PerformanceMetric::getEfficiency)
                .average()
                .orElse(0.0);

            int totalDowntime = metrics.stream()
                .filter(m -> m.getDowntime() != null)
                .mapToInt(PerformanceMetric::getDowntime)
                .sum();

            double failureRate = totalCount > 0 ? (failureCount.doubleValue() / totalCount) * 100 : 0.0;

            return AssetStatsResponseDTO.builder()
                .assetId(assetId)
                .assetName(asset.getAssetName())
                .averageTemperature(avgTemp)
                .averageVibration(avgVibration)
                .averageEfficiency(avgEfficiency)
                .totalDowntime(totalDowntime)
                .failureCount(failureCount.intValue())
                .failureRate(failureRate)
                .healthScore(asset.getHealthScore())
                .lastMetricTime(lastMetric.getTimestamp())
                .status(asset.getStatus())
                .build();
        });
    }

    /**
     * Get performance trends over time
     */
    public Mono<PerformanceTrendResponseDTO> getPerformanceTrend(String assetId, String period) {
        String cacheKey = cacheManager.getTrendKey(assetId, period);
        
        return cacheManager.getOrCompute(
            cacheKey,
            PerformanceTrendResponseDTO.class,
            () -> computePerformanceTrend(assetId, period),
            cacheManager.getAnalyticsTtl()
        );
    }

    private Mono<PerformanceTrendResponseDTO> computePerformanceTrend(String assetId, String period) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = switch (period) {
            case "24h" -> endTime.minusHours(24);
            case "7d" -> endTime.minusDays(7);
            case "30d" -> endTime.minusDays(30);
            default -> endTime.minusHours(24);
        };

        return metricRepository.findByAssetIdAndTimestampBetween(assetId, startTime, endTime)
            .collectList()
            .map(metrics -> {
                List<TimeSeriesDataDTO> tempTrend = metrics.stream()
                    .filter(m -> m.getTemperature() != null)
                    .map(m -> TimeSeriesDataDTO.builder()
                        .timestamp(m.getTimestamp())
                        .value(m.getTemperature())
                        .metric("temperature")
                        .build())
                    .collect(Collectors.toList());

                List<TimeSeriesDataDTO> efficiencyTrend = metrics.stream()
                    .filter(m -> m.getEfficiency() != null)
                    .map(m -> TimeSeriesDataDTO.builder()
                        .timestamp(m.getTimestamp())
                        .value(m.getEfficiency())
                        .metric("efficiency")
                        .build())
                    .collect(Collectors.toList());

                List<TimeSeriesDataDTO> downtimeTrend = metrics.stream()
                    .filter(m -> m.getDowntime() != null)
                    .map(m -> TimeSeriesDataDTO.builder()
                        .timestamp(m.getTimestamp())
                        .value(m.getDowntime().doubleValue())
                        .metric("downtime")
                        .build())
                    .collect(Collectors.toList());

                Map<String, Object> summary = new HashMap<>();
                summary.put("totalMetrics", metrics.size());
                summary.put("period", period);
                summary.put("startTime", startTime);
                summary.put("endTime", endTime);

                return PerformanceTrendResponseDTO.builder()
                    .assetId(assetId)
                    .period(period)
                    .temperatureTrend(tempTrend)
                    .efficiencyTrend(efficiencyTrend)
                    .downtimeTrend(downtimeTrend)
                    .summary(summary)
                    .build();
            });
    }

    /**
     * Compare performance between assets
     */
    public Mono<ComparativeAnalyticsResponseDTO> compareAssets(List<String> assetIds) {
        String cacheKey = cacheManager.getAnalyticsKey("compare-assets", assetIds.toArray(new String[0]));
        
        return cacheManager.getOrCompute(
            cacheKey,
            ComparativeAnalyticsResponseDTO.class,
            () -> computeAssetComparison(assetIds),
            cacheManager.getAnalyticsTtl()
        );
    }

    private Mono<ComparativeAnalyticsResponseDTO> computeAssetComparison(List<String> assetIds) {
        return Flux.fromIterable(assetIds)
            .flatMap(this::computeAssetStats)
            .collectMap(AssetStatsResponseDTO::getAssetId)
            .map(statsMap -> {
                Map<String, Object> insights = new HashMap<>();
                
                // Find best and worst performing assets
                var bestEfficiency = statsMap.values().stream()
                    .max(Comparator.comparing(AssetStatsResponseDTO::getAverageEfficiency))
                    .orElse(null);
                
                var worstEfficiency = statsMap.values().stream()
                    .min(Comparator.comparing(AssetStatsResponseDTO::getAverageEfficiency))
                    .orElse(null);

                insights.put("bestPerformer", bestEfficiency != null ? bestEfficiency.getAssetName() : "N/A");
                insights.put("worstPerformer", worstEfficiency != null ? worstEfficiency.getAssetName() : "N/A");
                insights.put("comparedAssets", assetIds.size());

                return ComparativeAnalyticsResponseDTO.builder()
                    .comparisonType("asset-vs-asset")
                    .comparison(statsMap)
                    .insights(insights)
                    .build();
            });
    }

    /**
     * Calculate health score for an asset based on recent metrics
     */
    public Mono<Double> calculateHealthScore(String assetId) {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        
        return metricRepository.findByAssetIdAndTimestampBetween(assetId, oneDayAgo, LocalDateTime.now())
            .collectList()
            .map(metrics -> {
                if (metrics.isEmpty()) {
                    return 100.0;
                }

                // Health score algorithm
                double score = 100.0;

                // Penalize for failures (max -30 points)
                long failureCount = metrics.stream()
                    .filter(m -> Boolean.TRUE.equals(m.getFailureDetected()))
                    .count();
                score -= Math.min(30, failureCount * 5);

                // Penalize for low efficiency (max -30 points)
                double avgEfficiency = metrics.stream()
                    .filter(m -> m.getEfficiency() != null)
                    .mapToDouble(PerformanceMetric::getEfficiency)
                    .average()
                    .orElse(100.0);
                if (avgEfficiency < 70) {
                    score -= (70 - avgEfficiency) * 0.5;
                }

                // Penalize for high temperature (max -20 points)
                double avgTemp = metrics.stream()
                    .filter(m -> m.getTemperature() != null)
                    .mapToDouble(PerformanceMetric::getTemperature)
                    .average()
                    .orElse(0.0);
                if (avgTemp > 80) {
                    score -= Math.min(20, (avgTemp - 80) * 0.5);
                }

                // Penalize for high vibration (max -20 points)
                double avgVibration = metrics.stream()
                    .filter(m -> m.getVibration() != null)
                    .mapToDouble(PerformanceMetric::getVibration)
                    .average()
                    .orElse(0.0);
                if (avgVibration > 5) {
                    score -= Math.min(20, (avgVibration - 5) * 2);
                }

                return Math.max(0, Math.min(100, score));
            })
            .flatMap(score -> 
                assetRepository.findById(assetId)
                    .flatMap(asset -> {
                        asset.setHealthScore(score);
                        return assetRepository.save(asset);
                    })
                    .thenReturn(score)
            );
    }

    /**
     * Demonstrate cache performance comparison
     */
    public Mono<CachePerformanceResponseDTO> getCachePerformanceDemo(String assetId) {
        long startCached = System.currentTimeMillis();
        
        return getAssetStats(assetId)
            .flatMap(cachedData -> {
                long cachedTime = System.currentTimeMillis() - startCached;
                
                long startNonCached = System.currentTimeMillis();
                return computeAssetStats(assetId)
                    .map(nonCachedData -> {
                        long nonCachedTime = System.currentTimeMillis() - startNonCached;
                        
                        return CachePerformanceResponseDTO.builder()
                            .cachedResponseTime(cachedTime)
                            .nonCachedResponseTime(nonCachedTime)
                            .speedupFactor((double) nonCachedTime / Math.max(1, cachedTime))
                            .fromCache(true)
                            .data(cachedData)
                            .build();
                    });
            });
    }
}
