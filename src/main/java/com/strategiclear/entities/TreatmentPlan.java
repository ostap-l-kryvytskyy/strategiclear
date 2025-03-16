package com.strategiclear.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "treatment_plan")
public class TreatmentPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "ACTION")
    private TreatmentAction action;

    @Column(name = "PATIENT")
    private String patientReference; // Посилання на пацієнта
    @Column(name = "START_TIME")
    private LocalDateTime startTime;
    @Column(name = "END_TIME")
    private LocalDateTime endTime; // Може бути null, якщо план нескінченний
    @Column(name = "RECURRENCE_PATTERN")
    private String recurrencePattern; // Формат рекурентності

    public TreatmentPlan() {}

    public TreatmentPlan(TreatmentAction treatmentAction, String patientReference, LocalDateTime startTime, LocalDateTime endTime, String recurrencePattern) {
        this.action = treatmentAction;
        this.patientReference = patientReference;
        this.startTime = startTime;
        this.endTime = endTime;
        this.recurrencePattern = recurrencePattern;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TreatmentAction getAction() {
        return action;
    }

    public void setAction(TreatmentAction action) {
        this.action = action;
    }

    public String getPatientReference() {
        return patientReference;
    }

    public void setPatientReference(String patientReference) {
        this.patientReference = patientReference;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getRecurrencePattern() {
        return recurrencePattern;
    }

    public void setRecurrencePattern(String recurrencePattern) {
        this.recurrencePattern = recurrencePattern;
    }
}