package com.astha.performance_dashboard.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricCreateRequestDTO {
    @NotBlank(message = "Asset ID is required")
    private String assetId;
    
    @NotNull(message = "Timestamp is required")
    private LocalDateTime timestamp;
    
    private Double temperature;
    private Double vibration;
    private Double powerConsumption;
    private Double efficiency;
    private Integer runningHours;
    private Integer downtime;
    private Boolean failureDetected;
    private String failureType;
    private String severity;
    private Map<String, Object> additionalMetrics;
}
