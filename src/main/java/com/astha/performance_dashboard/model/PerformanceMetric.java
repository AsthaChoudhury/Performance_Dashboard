package com.astha.performance_dashboard.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "performance_metrics")
@CompoundIndex(name = "asset_timestamp_idx", def = "{'assetId': 1, 'timestamp': -1}")
public class PerformanceMetric {
    
    @Id
    private String id;
    
    @Indexed
    private String assetId;
    
    @Indexed
    private LocalDateTime timestamp;
    
    private Double temperature;
    
    private Double vibration;
    
    private Double powerConsumption;
    
    private Double efficiency;
    
    private Integer runningHours;
    
    private Integer downtime; // in minutes
    
    private Boolean failureDetected;
    
    private String failureType;
    
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL
    
    private Map<String, Object> additionalMetrics;
    
    private String recordedBy;
}
