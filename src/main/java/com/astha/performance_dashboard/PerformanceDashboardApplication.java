package com.astha.performance_dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
// @EnableScheduling
// @EnableReactiveMongoAuditing
@ComponentScan(basePackages = "com.astha.performance_dashboard")
@EnableReactiveMongoRepositories(basePackages = "com.astha.performance_dashboard.repository")
public class PerformanceDashboardApplication {

	public static void main(String[] args) {
		SpringApplication.run(PerformanceDashboardApplication.class, args);
	}

}
