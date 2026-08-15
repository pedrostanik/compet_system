package com.petshop.api.history.service;

import com.petshop.api.history.dto.PetHistory;
import com.petshop.api.history.repository.HistoryRepository;
import com.petshop.api.schedule.domain.Scheduling;
import com.petshop.api.schedule.repository.SchedulingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final HistoryRepository historyRepository;

    public List<PetHistory> getPetHistory(Long petId) {
        return  historyRepository.getPetHistory(petId);
    }
}
