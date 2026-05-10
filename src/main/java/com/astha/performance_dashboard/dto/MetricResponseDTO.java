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
public class MetricResponseDTO {
    private String id;
    private String assetId;
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
}
