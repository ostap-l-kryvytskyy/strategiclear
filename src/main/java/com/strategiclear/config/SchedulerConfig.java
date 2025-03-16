package com.strategiclear.config;

import com.strategiclear.services.TreatmentTaskSchedulerService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@EnableAutoConfiguration
@EnableScheduling
public class SchedulerConfig {

    private final TreatmentTaskSchedulerService schedulerService;

    public SchedulerConfig(TreatmentTaskSchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    // Виконувати кожні 5 хвилин
    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void scheduleTasks() {
        schedulerService.scheduleTasks();
    }
}