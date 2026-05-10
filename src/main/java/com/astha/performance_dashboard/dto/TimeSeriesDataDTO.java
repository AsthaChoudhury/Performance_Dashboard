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
public class TimeSeriesDataDTO {
    private LocalDateTime timestamp;
    private Double value;
    private String metric;
}
