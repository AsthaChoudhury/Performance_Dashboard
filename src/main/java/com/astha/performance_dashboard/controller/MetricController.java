package com.astha.performance_dashboard.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.astha.performance_dashboard.dto.MetricCreateRequestDTO;
import com.astha.performance_dashboard.dto.MetricResponseDTO;
import com.astha.performance_dashboard.service.MetricService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
public class MetricController {

    private final MetricService metricService;

    @PostMapping
    public Mono<ResponseEntity<MetricResponseDTO>> createMetric(
            @Valid @RequestBody MetricCreateRequestDTO request) {
        return metricService.createMetric(request)
            .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
            .doOnNext(response -> log.info("Created metric via API: {}", response.getBody().getId()));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<MetricResponseDTO>> getMetric(@PathVariable String id) {
        return metricService.getMetric(id)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/asset/{assetId}")
    public Flux<MetricResponseDTO> getMetricsByAsset(@PathVariable String assetId) {
        return metricService.getMetricsByAsset(assetId);
    }

    @GetMapping("/asset/{assetId}/range")
    public Flux<MetricResponseDTO> getMetricsByAssetAndTimeRange(
            @PathVariable String assetId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return metricService.getMetricsByAssetAndTimeRange(assetId, startTime, endTime);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteMetric(@PathVariable String id) {
        return metricService.deleteMetric(id)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
