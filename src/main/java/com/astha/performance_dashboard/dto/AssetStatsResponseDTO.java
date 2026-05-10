package com.astha.performance_dashboard.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetStatsResponseDTO {
    private String assetId;
    private String assetName;
    private Double averageTemperature;
    private Double averageVibration;
    private Double averageEfficiency;
    private Integer totalDowntime;
    private Integer failureCount;
    private Double failureRate;
    private Double healthScore;
    private LocalDateTime lastMetricTime;
    private String status;
}
