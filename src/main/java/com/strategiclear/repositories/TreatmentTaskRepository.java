package com.strategiclear.repositories;

import com.strategiclear.entities.TreatmentTask;
import com.strategiclear.entities.TreatmentPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface TreatmentTaskRepository extends JpaRepository<TreatmentTask, Long> {
}
