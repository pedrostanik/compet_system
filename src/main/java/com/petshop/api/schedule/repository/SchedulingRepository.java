package com.petshop.api.schedule.repository;

import com.petshop.api.schedule.domain.Scheduling;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SchedulingRepository extends JpaRepository<Scheduling, Long> {

    List<Scheduling> findByCustomerId(Long customerId);

    List<Scheduling> findByPetId(Long petId);

    List<Scheduling> findByTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query(value = """
    SELECT * FROM schedulings s 
    WHERE s.time < :end 
    AND (s.time + (s.duration || ' minutes')::interval) > :start
""", nativeQuery = true)
    List<Scheduling> findOverlapping(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = """
    SELECT EXISTS (
        SELECT 1 FROM schedulings s 
        WHERE s.id <> :id 
        AND s.happened = false 
        AND s.time < :requestedEnd 
        AND (s.time + (s.duration || ' minutes')::interval) > :requestedStart
    )
""", nativeQuery = true)
    boolean existsOverlapping(
            @Param("id") Long id,
            @Param("requestedStart") LocalDateTime start,
            @Param("requestedEnd") LocalDateTime end
    );

}
