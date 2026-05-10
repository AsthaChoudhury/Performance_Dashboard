package com.astha.performance_dashboard.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceTrendResponseDTO {
    private String assetId;
    private String period; 
    private List<TimeSeriesDataDTO> temperatureTrend;
    private List<TimeSeriesDataDTO> efficiencyTrend;
    private List<TimeSeriesDataDTO> downtimeTrend;
    private Map<String, Object> summary;
}
