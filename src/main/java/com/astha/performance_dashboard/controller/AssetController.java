package com.astha.performance_dashboard.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.astha.performance_dashboard.dto.AssetCreateRequestDTO;
import com.astha.performance_dashboard.dto.AssetResponseDTO;
import com.astha.performance_dashboard.dto.AssetUpdateRequestDTO;
import com.astha.performance_dashboard.service.AssetService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @PostMapping
    public Mono<ResponseEntity<AssetResponseDTO>> createAsset(
            @Valid @RequestBody AssetCreateRequestDTO request) {
        return assetService.createAsset(request)
            .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
            .doOnNext(response -> log.info("Created asset via API: {}", response.getBody().getId()));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<AssetResponseDTO>> getAsset(@PathVariable String id) {
        return assetService.getAsset(id)
            .map(ResponseEntity::ok);
    }

    @GetMapping
    public Flux<AssetResponseDTO> getAllAssets(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {
        
        if (status != null) {
            return assetService.getAssetsByStatus(status);
        }
        if (type != null) {
            return assetService.getAssetsByType(type);
        }
        return assetService.getAllAssets();
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<AssetResponseDTO>> updateAsset(
            @PathVariable String id,
            @Valid @RequestBody AssetUpdateRequestDTO request) {
        return assetService.updateAsset(id, request)
            .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteAsset(@PathVariable String id) {
        return assetService.deleteAsset(id)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @GetMapping("/stats/count")
    public Mono<ResponseEntity<Map<String, Long>>> getAssetCount() {
        return assetService.getAssetCount()
            .map(count -> ResponseEntity.ok(Map.of("totalAssets", count)));
    }

    @GetMapping("/health/low")
    public Flux<AssetResponseDTO> getLowHealthAssets(
            @RequestParam(defaultValue = "70.0") Double threshold) {
        return assetService.getAssetsWithLowHealth(threshold);
    }
}
