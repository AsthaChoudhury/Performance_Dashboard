package com.astha.performance_dashboard.service;

import com.astha.performance_dashboard.dto.*;
import com.astha.performance_dashboard.dto.AssetResponseDTO;
import com.astha.performance_dashboard.exception.ResourceNotFoundException;
import com.astha.performance_dashboard.model.Asset;
import com.astha.performance_dashboard.repository.AssetRepository;
import com.astha.performance_dashboard.utils.Cachemanager;
import com.astha.performance_dashboard.websocket.WebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;
    private final Cachemanager cacheManager;
    private final WebSocketHandler webSocketHandler;

    public Mono<AssetResponseDTO> createAsset(AssetCreateRequestDTO request) {
        Asset asset = Asset.builder()
            .assetName(request.getAssetName())
            .assetType(request.getAssetType())
            .location(request.getLocation())
            .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
            .installationDate(request.getInstallationDate())
            .specifications(request.getSpecifications())
            .manufacturer(request.getManufacturer())
            .model(request.getModel())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .healthScore(100.0)
            .build();

        return assetRepository.save(asset)
            .map(this::toResponse)
            .doOnNext(response -> log.info("Created asset: {}", response.getId()))
            .flatMap(response -> {
                // Invalidate relevant caches
                return cacheManager.invalidatePattern("asset:*")
                    .thenReturn(response);
            });
    }

    public Mono<AssetResponseDTO> getAsset(String id) {
        String cacheKey = "asset:" + id;
        
        return cacheManager.getOrCompute(
            cacheKey,
            Asset.class,
            () -> assetRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Asset not found: " + id))),
            cacheManager.getAssetStatsTtl()
        ).map(this::toResponse);
    }

    public Flux<AssetResponseDTO> getAllAssets() {
        return assetRepository.findAll()
            .map(this::toResponse);
    }

    public Flux<AssetResponseDTO> getAssetsByStatus(String status) {
        return assetRepository.findByStatus(status)
            .map(this::toResponse);
    }

    public Flux<AssetResponseDTO> getAssetsByType(String type) {
        return assetRepository.findByAssetType(type)
            .map(this::toResponse);
    }

    public Mono<AssetResponseDTO> updateAsset(String id, AssetUpdateRequestDTO request) {
        return assetRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Asset not found: " + id)))
            .flatMap(asset -> {
                if (request.getAssetName() != null) asset.setAssetName(request.getAssetName());
                if (request.getAssetType() != null) asset.setAssetType(request.getAssetType());
                if (request.getLocation() != null) asset.setLocation(request.getLocation());
                if (request.getStatus() != null) asset.setStatus(request.getStatus());
                if (request.getLastMaintenanceDate() != null) 
                    asset.setLastMaintenanceDate(request.getLastMaintenanceDate());
                if (request.getSpecifications() != null) 
                    asset.setSpecifications(request.getSpecifications());
                
                asset.setUpdatedAt(LocalDateTime.now());
                
                return assetRepository.save(asset);
            })
            .map(this::toResponse)
            .flatMap(response -> {
                // Invalidate cache for this asset
                return cacheManager.invalidate("asset:" + id)
                    .then(cacheManager.invalidatePattern("asset:stats:" + id + "*"))
                    .thenReturn(response);
            })
            .doOnNext(response -> {
                // Broadcast status change via WebSocket
                if (request.getStatus() != null) {
                    WebSocketMessageDTO message = WebSocketMessageDTO.builder()
                        .type("STATUS_CHANGE")
                        .assetId(id)
                        .payload(Map.of("newStatus", request.getStatus()))
                        .timestamp(LocalDateTime.now())
                        .build();
                    webSocketHandler.broadcast(message);
                }
            });
    }

    public Mono<Void> deleteAsset(String id) {
        return assetRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Asset not found: " + id)))
            .flatMap(asset -> assetRepository.delete(asset))
            .then(cacheManager.invalidatePattern("asset*"))
            .then()
            .doOnSuccess(v -> log.info("Deleted asset: {}", id));
    }

    public Mono<Long> getAssetCount() {
        return assetRepository.count();
    }

    public Flux<AssetResponseDTO> getAssetsWithLowHealth(Double threshold) {
        return assetRepository.findAssetsWithLowHealth(threshold)
            .map(this::toResponse);
    }

    private AssetResponseDTO toResponse(Asset asset) {
        return AssetResponseDTO.builder()
            .id(asset.getId())
            .assetName(asset.getAssetName())
            .assetType(asset.getAssetType())
            .location(asset.getLocation())
            .status(asset.getStatus())
            .installationDate(asset.getInstallationDate())
            .lastMaintenanceDate(asset.getLastMaintenanceDate())
            .healthScore(asset.getHealthScore())
            .manufacturer(asset.getManufacturer())
            .model(asset.getModel())
            .createdAt(asset.getCreatedAt())
            .updatedAt(asset.getUpdatedAt())
            .build();
    }
}