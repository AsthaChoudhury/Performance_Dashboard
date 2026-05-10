package com.astha.performance_dashboard.dto;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComparativeAnalyticsResponseDTO {
    private String comparisonType; // asset-vs-asset, period-vs-period
    private Map<String, AssetStatsResponseDTO> comparison;
    private Map<String, Object> insights;
}
