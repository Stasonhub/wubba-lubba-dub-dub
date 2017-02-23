package com.airent.config;

import com.airent.service.provider.AdvertImportService;
import com.airent.service.provider.ProviderScheduler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SchedulerConfig {

    @Bean
    public ProviderScheduler providerScheduler(AdvertImportService advertImportService,
                                               @Value("${import.schedule.initial}") long importScheduleInitial,
                                               @Value("${import.schedule.interval}") long importScheduleInterval) {
        ProviderScheduler providerScheduler = new ProviderScheduler(advertImportService);
        providerScheduler.schedule(importScheduleInitial, importScheduleInterval);
        return providerScheduler;
    }

}