package com.astha.performance_dashboard.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ExportRequestDTO {
        private String assetId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String format; // JSON, CSV
    private List<String> metrics;
}
