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

public class WebSocketMessageDTO {
        private String type; // METRIC_UPDATE, ALERT, STATUS_CHANGE, HEARTBEAT
    private String assetId;
    private Object payload;
    private LocalDateTime timestamp;
}
