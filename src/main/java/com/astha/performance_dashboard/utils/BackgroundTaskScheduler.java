package com.astha.performance_dashboard.utils;

import com.astha.performance_dashboard.repository.AssetRepository;
import com.astha.performance_dashboard.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class BackgroundTaskScheduler {

    private final AnalyticsService analyticsService;
    private final AssetRepository assetRepository;

    @Scheduled(cron = "${tasks.aggregation-cron:0 */5 * * * *}")
    public void calculateHealthScores() {
        log.info("Starting scheduled health score calculation...");
        
        assetRepository.findAll()
            .flatMap(asset -> 
                analyticsService.calculateHealthScore(asset.getId())
                    .doOnNext(score -> 
                        log.debug("Updated health score for asset {}: {}", asset.getAssetName(), score)
                    )
                    .onErrorResume(error -> {
                        log.error("Failed to calculate health score for asset {}", asset.getId(), error);
                        return Mono.empty();
                    })
            )
            .subscribe(
                null,
                error -> log.error("Health score calculation task failed", error),
                () -> log.info("Completed health score calculation for all assets")
            );
    }


    @Scheduled(cron = "${tasks.report-generation-cron:0 0 * * * *}")
    public void logSystemMetrics() {
        log.info("Generating hourly system report...");
        
        assetRepository.count()
            .subscribe(count -> 
                log.info("System Report - Total Assets: {}", count)
            );
    }
}