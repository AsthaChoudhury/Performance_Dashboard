package com.astha.performance_dashboard.repository;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.astha.performance_dashboard.model.Asset;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AssetRepository extends ReactiveMongoRepository<Asset, String> {
    Mono<Asset> findByAssetName(String assetName);
    Flux<Asset> findByStatus(String status);
    Flux<Asset> findByAssetType(String assetType);
    Flux<Asset> findByLocation(String location);
    Flux<Asset> findByOwnerId(String ownerId);
    
    @Query("{'healthScore': {$lt: ?0}}")
    Flux<Asset> findAssetsWithLowHealth(Double threshold);
}

