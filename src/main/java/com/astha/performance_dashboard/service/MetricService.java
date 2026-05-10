package com.astha.performance_dashboard.service;

import com.astha.performance_dashboard.dto.*;
import com.astha.performance_dashboard.exception.ResourceNotFoundException;
import com.astha.performance_dashboard.model.PerformanceMetric;
import com.astha.performance_dashboard.repository.PerformanceMetricRepository;
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
public class MetricService {

    private final PerformanceMetricRepository metricRepository;
    private final Cachemanager cacheManager;
    private final WebSocketHandler webSocketHandler;

    public Mono<MetricResponseDTO> createMetric(MetricCreateRequestDTO request) {
        PerformanceMetric metric = PerformanceMetric.builder()
            .assetId(request.getAssetId())
            .timestamp(request.getTimestamp())
            .temperature(request.getTemperature())
            .vibration(request.getVibration())
            .powerConsumption(request.getPowerConsumption())
            .efficiency(request.getEfficiency())
            .runningHours(request.getRunningHours())
            .downtime(request.getDowntime())
            .failureDetected(request.getFailureDetected())
            .failureType(request.getFailureType())
            .severity(request.getSeverity())
            .additionalMetrics(request.getAdditionalMetrics())
            .build();

        return metricRepository.save(metric)
            .map(this::toResponse)
            .doOnNext(response -> {
                log.info("Created metric for asset: {}", response.getAssetId());
                
                // Invalidate cache for this asset
                cacheManager.invalidatePattern("asset:stats:" + request.getAssetId() + "*").subscribe();
                cacheManager.invalidatePattern("trend:" + request.getAssetId() + "*").subscribe();
                
                // Broadcast metric update via WebSocket
                WebSocketMessageDTO wsMessage = WebSocketMessageDTO.builder()
                    .type("METRIC_UPDATE")
                    .assetId(request.getAssetId())
                    .payload(response)
                    .timestamp(LocalDateTime.now())
                    .build();
                webSocketHandler.broadcastToAsset(request.getAssetId(), wsMessage);
                
                // Check for alerts
                checkAndSendAlerts(metric);
            });
    }

    public Mono<MetricResponseDTO> getMetric(String id) {
        return metricRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Metric not found: " + id)))
            .map(this::toResponse);
    }

    public Flux<MetricResponseDTO> getMetricsByAsset(String assetId) {
        return metricRepository.findByAssetIdOrderByTimestampDesc(assetId)
            .map(this::toResponse);
    }

    public Flux<MetricResponseDTO> getMetricsByAssetAndTimeRange(
            String assetId, 
            LocalDateTime startTime, 
            LocalDateTime endTime) {
        return metricRepository.findByAssetIdAndTimestampBetween(assetId, startTime, endTime)
            .map(this::toResponse);
    }

    public Mono<Void> deleteMetric(String id) {
        return metricRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Metric not found: " + id)))
            .flatMap(metric -> {
                String assetId = metric.getAssetId();
                return metricRepository.delete(metric)
                    .then(cacheManager.invalidatePattern("asset:stats:" + assetId + "*"))
                    .then();
            })
            .doOnSuccess(v -> log.info("Deleted metric: {}", id));
    }

    private void checkAndSendAlerts(PerformanceMetric metric) {
        // Check for high temperature alert
        if (metric.getTemperature() != null && metric.getTemperature() > 90) {
            sendAlert(metric.getAssetId(), "HIGH_TEMPERATURE", "CRITICAL",
                "Temperature exceeded 90°C: " + metric.getTemperature() + "°C",
                Map.of("temperature", metric.getTemperature(), "threshold", 90));
        }

        // Check for failure alert
        if (Boolean.TRUE.equals(metric.getFailureDetected())) {
            sendAlert(metric.getAssetId(), "FAILURE_DETECTED", 
                metric.getSeverity() != null ? metric.getSeverity() : "MEDIUM",
                "Failure detected: " + (metric.getFailureType() != null ? metric.getFailureType() : "Unknown"),
                Map.of("failureType", metric.getFailureType()));
        }

        // Check for low efficiency alert
        if (metric.getEfficiency() != null && metric.getEfficiency() < 50) {
            sendAlert(metric.getAssetId(), "LOW_EFFICIENCY", "MEDIUM",
                "Efficiency dropped below 50%: " + metric.getEfficiency() + "%",
                Map.of("efficiency", metric.getEfficiency(), "threshold", 50));
        }

        // Check for high vibration alert
        if (metric.getVibration() != null && metric.getVibration() > 8) {
            sendAlert(metric.getAssetId(), "HIGH_VIBRATION", "HIGH",
                "Vibration exceeded safe levels: " + metric.getVibration(),
                Map.of("vibration", metric.getVibration(), "threshold", 8));
        }
    }

    private void sendAlert(String assetId, String alertType, String severity, String message, Map<String, Object> details) {
        PerformanceAlertDTO alert = PerformanceAlertDTO.builder()
            .assetId(assetId)
            .alertType(alertType)
            .severity(severity)
            .message(message)
            .timestamp(LocalDateTime.now())
            .details(details)
            .build();

        WebSocketMessageDTO wsMessage = WebSocketMessageDTO.builder()
            .type("ALERT")
            .assetId(assetId)
            .payload(alert)
            .timestamp(LocalDateTime.now())
            .build();

        webSocketHandler.broadcastToAsset(assetId, wsMessage);
        log.warn("Alert sent for asset {}: {}", assetId, message);
    }

    private MetricResponseDTO toResponse(PerformanceMetric metric) {
        return MetricResponseDTO.builder()
            .id(metric.getId())
            .assetId(metric.getAssetId())
            .timestamp(metric.getTimestamp())
            .temperature(metric.getTemperature())
            .vibration(metric.getVibration())
            .powerConsumption(metric.getPowerConsumption())
            .efficiency(metric.getEfficiency())
            .runningHours(metric.getRunningHours())
            .downtime(metric.getDowntime())
            .failureDetected(metric.getFailureDetected())
            .failureType(metric.getFailureType())
            .severity(metric.getSeverity())
            .build();
    }
}
