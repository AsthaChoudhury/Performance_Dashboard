package com.astha.performance_dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetResponseDTO {
    private String id;
    private String assetName;
    private String assetType;
    private String location;
    private String status;
    private LocalDateTime installationDate;
    private LocalDateTime lastMaintenanceDate;
    private Double healthScore;
    private String manufacturer;
    private String model;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
