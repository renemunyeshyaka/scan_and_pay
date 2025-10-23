package com.scan_and_pay.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.scan_and_pay.repositories")
public class DataSourceConfig {
    // Let Spring Boot auto-configure everything
}