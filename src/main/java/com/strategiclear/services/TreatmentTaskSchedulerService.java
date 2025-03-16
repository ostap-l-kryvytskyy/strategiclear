package com.strategiclear.services;

import com.strategiclear.entities.*;
import com.strategiclear.repositories.TreatmentPlanRepository;
import com.strategiclear.repositories.TreatmentTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class TreatmentTaskSchedulerService {

    private final TreatmentPlanRepository treatmentPlanRepository;
    private final TreatmentTaskRepository treatmentTaskRepository;

    public TreatmentTaskSchedulerService(TreatmentPlanRepository treatmentPlanRepository, TreatmentTaskRepository treatmentTaskRepository) {
        this.treatmentPlanRepository = treatmentPlanRepository;
        this.treatmentTaskRepository = treatmentTaskRepository;
    }

    @Transactional
    public void scheduleTasks() {
        System.out.println("Analysing tasks now at " + LocalDateTime.now() + "...");
        // Отримуємо всі активні Treatment Plans
        List<TreatmentPlan> plans = treatmentPlanRepository.findAll();

        for (TreatmentPlan plan : plans) {
            generateTasksForPlan(plan);
        }
    }

    private void generateTasksForPlan(TreatmentPlan plan) {
        LocalDateTime now = LocalDateTime.now();

        // Ігнорувати плани, кінцевий час яких вже завершився
        if (plan.getEndTime() != null && plan.getEndTime().isBefore(now)) {
            return;
        }

        try {
            // [Простий приклад] Парсинг рекурентного паттерну (наприклад "08:00")
            String[] times = plan.getRecurrencePattern().split(",");
            for (String time : times) {
                LocalDateTime taskTime = calculateNextOccurrence(now, plan.getStartTime(), time.trim());
                if (taskTime != null && taskTime.isBefore(now.plusHours(1))) { // Task на найближчу годину
                    TreatmentTask task = new TreatmentTask(
                        plan,
                        taskTime
                    );
                    treatmentTaskRepository.save(task);
                }
            }
        } catch (DateTimeParseException e) {
            // Логування помилки парсингу рекурентного паттерну
        }
    }

    public LocalDateTime calculateNextOccurrence(LocalDateTime now, LocalDateTime planStart, String time) {
        // Проста логіка для демонстрації - додати час до дня
        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        LocalDateTime candidate = planStart.withHour(hour).withMinute(minute);
        return candidate.isAfter(now) ? candidate : null;
    }
}