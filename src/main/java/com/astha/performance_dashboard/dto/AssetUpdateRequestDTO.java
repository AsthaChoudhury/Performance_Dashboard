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
public class AssetUpdateRequestDTO {
    private String assetName;
    private String assetType;
    private String location;
    private String status;
    private LocalDateTime lastMaintenanceDate;
    private Map<String, Object> specifications;
}