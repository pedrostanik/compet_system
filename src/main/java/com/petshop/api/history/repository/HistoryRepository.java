package com.petshop.api.history.repository;

import com.petshop.api.history.dto.PetHistory;
import com.petshop.api.schedule.domain.Scheduling;
import com.petshop.api.schedule.domain.enums.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}