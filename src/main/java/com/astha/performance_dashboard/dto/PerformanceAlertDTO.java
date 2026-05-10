package com.astha.performance_dashboard.dto;
import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceAlertDTO {
    private String assetId;
    private String assetName;
    private String alertType; // HIGH_TEMPERATURE, HIGH_FAILURE_RATE, LOW_EFFICIENCY
    private String severity;
    private String message;
    private LocalDateTime timestamp;
    private Map<String, Object> details;
}
