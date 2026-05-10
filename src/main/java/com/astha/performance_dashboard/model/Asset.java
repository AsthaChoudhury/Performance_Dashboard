package com.astha.performance_dashboard.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "assets")
public class Asset {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String assetName;
    
    private String assetType;
    
    private String location;
    
    private String status; // ACTIVE, MAINTENANCE, INACTIVE
    
    private LocalDateTime installationDate;
    
    private LocalDateTime lastMaintenanceDate;
    
    private Map<String, Object> specifications;
    
    private String ownerId;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private Double healthScore; // 0-100
    
    private String manufacturer;
    
    private String model;
}
