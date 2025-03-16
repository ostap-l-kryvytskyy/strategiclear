package com.strategiclear.tests;

import com.strategiclear.entities.TreatmentAction;
import com.strategiclear.entities.TreatmentPlan;
import com.strategiclear.entities.TreatmentTask;
import com.strategiclear.repositories.TreatmentPlanRepository;
import com.strategiclear.repositories.TreatmentTaskRepository;
import com.strategiclear.services.TreatmentTaskSchedulerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class TreatmentTaskSchedulerServiceTest {

    @Autowired
    private TreatmentPlanRepository treatmentPlanRepository;

    @Autowired
    private TreatmentTaskRepository treatmentTaskRepository;

    @Autowired
    private TreatmentTaskSchedulerService treatmentTaskSchedulerService;

    @AfterEach
    void tearDown() {
        treatmentTaskRepository.deleteAll();
        treatmentPlanRepository.deleteAll();
    }

    @Test
    void testScheduleTasks_WithNoPlans() {
        // Given: No treatment plans in the database

        // When: scheduleTasks is executed
        treatmentTaskSchedulerService.scheduleTasks();

        // Then: No tasks should be created or saved
        List<TreatmentTask> all = treatmentTaskRepository.findAll();
        assertEquals(0, all.size());
        System.out.println("There is no tasks created since there is no plans in the database.");
    }

    @Test
    void testScheduleTasks_WithSinglePlan() {
        // Given: A treatment plan with recurrence every day at 08:00
        TreatmentPlan plan = new TreatmentPlan(
                TreatmentAction.ACTION_A,
                "patient-001",
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1),
                "08:00"
        );

        treatmentPlanRepository.save(plan);

        // When: We call the scheduler
        treatmentTaskSchedulerService.scheduleTasks();

        String[] times = plan.getRecurrencePattern().split(",");
        for (String time : times) {
            LocalDateTime taskTime = treatmentTaskSchedulerService.calculateNextOccurrence(LocalDateTime.now(), plan.getStartTime(), time.trim());
            if (taskTime != null && taskTime.isBefore(LocalDateTime.now().plusHours(1))) { // Task на найближчу годину
                // Then: A task should be created and saved
                List<TreatmentTask> all = treatmentTaskRepository.findAll();
                assertEquals(1, all.size());

                TreatmentTask savedTask = all.get(0);
                assertNotNull(savedTask);
                assertEquals(plan.getId(), savedTask.getTreatmentPlan().getId());
                assertEquals(plan.getPatientReference(), savedTask.getTreatmentPlan().getPatientReference());
                System.out.println("There is 1 task active in 1 hour timeframe.");
            } else {
                // Then: A task should not be created and saved
                List<TreatmentTask> all = treatmentTaskRepository.findAll();
                assertEquals(0, all.size());
                System.out.println("There is no task active in 1 hour timeframe.");
            }
        }
    }

    @Test
    void testScheduleTasks_WithSinglePlanMultipleRecurrencePatterns() {
        // Given: A treatment plan with recurrence every day at 08:00
        TreatmentPlan plan = new TreatmentPlan(
                TreatmentAction.ACTION_A,
                "patient-001",
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1),
                "08:05, 08:10, 08:15"
        );

        treatmentPlanRepository.save(plan);

        // When: We call the scheduler
        treatmentTaskSchedulerService.scheduleTasks();

        String[] times = plan.getRecurrencePattern().split(",");
        for (String time : times) {
            LocalDateTime taskTime = treatmentTaskSchedulerService.calculateNextOccurrence(LocalDateTime.now(), plan.getStartTime(), time.trim());
            if (taskTime != null && taskTime.isBefore(LocalDateTime.now().plusHours(1))) { // Task на найближчу годину
                // Then: A 3 tasks should be created and saved
                List<TreatmentTask> all = treatmentTaskRepository.findAll();
                assertEquals(3, all.size());

                TreatmentTask savedTask = all.get(0);
                assertNotNull(savedTask);
                assertEquals(plan.getId(), savedTask.getTreatmentPlan().getId());
                assertEquals(plan.getPatientReference(), savedTask.getTreatmentPlan().getPatientReference());
                System.out.println("There is 1 task active in 1 hour timeframe.");

                TreatmentTask savedTask1 = all.get(1);
                assertNotNull(savedTask1);
                assertEquals(plan.getId(), savedTask1.getTreatmentPlan().getId());
                assertEquals(plan.getPatientReference(), savedTask1.getTreatmentPlan().getPatientReference());
                System.out.println("There is 2nd task active in 1 hour timeframe.");

                TreatmentTask savedTask2 = all.get(2);
                assertNotNull(savedTask2);
                assertEquals(plan.getId(), savedTask2.getTreatmentPlan().getId());
                assertEquals(plan.getPatientReference(), savedTask2.getTreatmentPlan().getPatientReference());
                System.out.println("There is 3rd task active in 1 hour timeframe.");
            } else {
                // Then: A task should not be created and saved
                List<TreatmentTask> all = treatmentTaskRepository.findAll();
                assertEquals(0, all.size());
                System.out.println("There is no task active in 1 hour timeframe.");
            }
        }
    }

    @Test
    void testScheduleTasks_PastPlanShouldNotGenerateTasks() {
        // Given: A treatment plan that has already ended
        TreatmentPlan pastPlan = new TreatmentPlan(
                TreatmentAction.ACTION_A,
                "patient-002",
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().minusDays(5),
                "08:00"
        );

        treatmentPlanRepository.save(pastPlan);

        // When: The scheduler is executed
        treatmentTaskSchedulerService.scheduleTasks();

        // Then: No tasks should be created
        List<TreatmentTask> all = treatmentTaskRepository.findAll();
        assertEquals(0, all.size());
        System.out.println("There is no tasks created since all the plans past due in the database.");
    }

    @Test
    void testScheduleTasks_HandlesMultiplePlans() {
        // Given: Two active plans
        TreatmentPlan plan1 = new TreatmentPlan(
                TreatmentAction.ACTION_A,
                "patient-001",
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1),
                "08:00"
        );

        TreatmentPlan plan2 = new TreatmentPlan(
                TreatmentAction.ACTION_B,
                "patient-002",
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(2),
                "08:10"
        );

        treatmentPlanRepository.save(plan1);
        treatmentPlanRepository.save(plan2);

        // When: The scheduler is executed
        treatmentTaskSchedulerService.scheduleTasks();

        String[] times1 = plan1.getRecurrencePattern().split(",");
        for (String time : times1) {
            LocalDateTime taskTime = treatmentTaskSchedulerService.calculateNextOccurrence(LocalDateTime.now(), plan1.getStartTime(), time.trim());
            if (taskTime != null && taskTime.isBefore(LocalDateTime.now().plusHours(1))) { // Task на найближчу годину
                // Then: A task should be created and saved
                List<TreatmentTask> all = treatmentTaskRepository.findAll();
                assertEquals(2, all.size());

                TreatmentTask savedTask = all.get(0);
                assertNotNull(savedTask);
                assertEquals(plan1.getId(), savedTask.getTreatmentPlan().getId());
                assertEquals(plan1.getPatientReference(), savedTask.getTreatmentPlan().getPatientReference());
                System.out.println("There is 1 task active in plan1 in 1 hour timeframe.");
            } else {
                // Then: A task should not be created and saved
                List<TreatmentTask> all = treatmentTaskRepository.findAll();
                assertEquals(0, all.size());
                System.out.println("There is no task active in plan1 in 1 hour timeframe.");
            }
        }

        String[] times2 = plan2.getRecurrencePattern().split(",");
        for (String time : times2) {
            LocalDateTime taskTime = treatmentTaskSchedulerService.calculateNextOccurrence(LocalDateTime.now(), plan2.getStartTime(), time.trim());
            if (taskTime != null && taskTime.isBefore(LocalDateTime.now().plusHours(1))) { // Task на найближчу годину
                // Then: A task should be created and saved
                List<TreatmentTask> all = treatmentTaskRepository.findAll();
                assertEquals(2, all.size());

                TreatmentTask savedTask = all.get(1);
                assertNotNull(savedTask);
                assertEquals(plan2.getId(), savedTask.getTreatmentPlan().getId());
                assertEquals(plan2.getPatientReference(), savedTask.getTreatmentPlan().getPatientReference());
                System.out.println("There is 1 task active in plan2 in 1 hour timeframe.");
            } else {
                // Then: A task should not be created and saved
                List<TreatmentTask> all = treatmentTaskRepository.findAll();
                assertEquals(0, all.size());
                System.out.println("There is no task active in plan2 in 1 hour timeframe.");
            }
        }
    }
}