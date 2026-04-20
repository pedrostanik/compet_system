package com.petshop.api.schedule.repository;

import com.petshop.api.schedule.domain.Scheduling;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SchedulingRepository extends JpaRepository<Scheduling, Long> {

    List<Scheduling> findByCustomerId(Long customerId);

    List<Scheduling> findByPetId(Long petId);

    List<Scheduling> findByTimeBetween(LocalDateTime start, LocalDateTime end);
}
