package com.petshop.api.history.controller;

import com.petshop.api.history.dto.PetHistory;
import com.petshop.api.history.service.HistoryService;
import com.petshop.api.schedule.dto.SchedulingResponse;
import com.petshop.api.schedule.service.SchedulingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping("/{id}")
    public List<PetHistory> get(@PathVariable Long id) {
        return historyService.getPetHistory(id);
    }

    @GetMapping("/frequency/{id}")
    public List<Object[]> getFrequency(@PathVariable Long id) {
        return historyService.getFrequency(id);
    }
}
