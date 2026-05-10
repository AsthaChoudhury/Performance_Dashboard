package com.astha.performance_dashboard.repository;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.astha.performance_dashboard.model.PerformanceMetric;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PerformanceMetricRepository extends ReactiveMongoRepository<PerformanceMetric, String> {
    Flux<PerformanceMetric> findByAssetIdOrderByTimestampDesc(String assetId);
    
    Flux<PerformanceMetric> findByAssetIdAndTimestampBetween(
        String assetId, 
        LocalDateTime start, 
        LocalDateTime end
    );
    
    Flux<PerformanceMetric> findByTimestampBetween(
        LocalDateTime start, 
        LocalDateTime end
    );
    
    @Query("{'assetId': ?0, 'failureDetected': true}")
    Flux<PerformanceMetric> findFailuresByAssetId(String assetId);
    
    // @Query("{'assetId': ?0}")
    // Mono<Long> countByAssetId(String assetId);
    Mono<Long> countByAssetId(String assetId);
    
    Mono<Long> countByAssetIdAndFailureDetectedTrue(String assetId);
    
    @Query("{'assetId': ?0, 'failureDetected': true}")
    Mono<Long> countFailuresByAssetId(String assetId);
    
    Mono<PerformanceMetric> findFirstByAssetIdOrderByTimestampDesc(String assetId);
}

