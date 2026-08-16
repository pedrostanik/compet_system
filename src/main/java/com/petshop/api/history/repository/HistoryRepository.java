package com.petshop.api.history.repository;

import com.petshop.api.history.dto.PetHistory;
import com.petshop.api.schedule.domain.Scheduling;
import com.petshop.api.schedule.domain.enums.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface HistoryRepository extends JpaRepository<Scheduling, Long> {

    @Query("""
        SELECT new com.petshop.api.history.dto.PetHistory(
            s.time, 
            s.price, 
            s.duration, 
            s.schedulingObservations
        )
        FROM Scheduling s 
        WHERE s.petId = :petId 
        AND s.scheduleStatus = com.petshop.api.schedule.domain.enums.ScheduleStatus.HAPPENED 
        ORDER BY s.time ASC    
        """)
    List<PetHistory> getPetHistory(
            @Param("petId") Long petId
    );

    @Query(value = """
    SELECT DATE_TRUNC('month', s.time) AS date,
           COUNT(s.id) AS frequency
    FROM schedulings s 
    WHERE s.pet_id = :petId 
      AND s.schedule_status = 'HAPPENED' 
      AND s.time >= NOW() - INTERVAL '12 months'
    GROUP BY DATE_TRUNC('month', s.time)
    ORDER BY date ASC
    """, nativeQuery = true)
    List<Object[]> getPetFrequency(
            @Param("petId") Long petId
    );
}