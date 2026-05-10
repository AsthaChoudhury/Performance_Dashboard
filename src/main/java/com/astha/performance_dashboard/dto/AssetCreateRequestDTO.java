package com.astha.performance_dashboard.dto;

import java.time.LocalDateTime;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetCreateRequestDTO {
    
    @NotBlank(message = "Asset name is required")
    private String assetName;
    
    @NotBlank(message = "Asset type is required")
    private String assetType;
    
    private String location;
    private String status;
    private LocalDateTime installationDate;
    private Map<String, Object> specifications;
    private String manufacturer;
    private String model;
}

