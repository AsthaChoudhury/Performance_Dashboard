package com.astha.performance_dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class CachePerformanceResponseDTO {
        private Long cachedResponseTime;
    private Long nonCachedResponseTime;
    private Double speedupFactor;
    private Boolean fromCache;
    private Object data;
}
