package com.petshop.api.schedule.controller;

import com.petshop.api.schedule.dto.SchedulingRequest;
import com.petshop.api.schedule.dto.SchedulingResponse;
import com.petshop.api.schedule.service.SchedulingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scheduling")
@RequiredArgsConstructor
public class SchedulingController {

    private final SchedulingService schedulingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SchedulingResponse create(@RequestBody SchedulingRequest request) {
        return schedulingService.createScheduling(request);
    }

    @GetMapping
    public List<SchedulingResponse> listCustomers() {
        return schedulingService.listScheduling();
    }

    @GetMapping("/{id}")
    public SchedulingResponse get(@PathVariable Long id) {
        return schedulingService.getScheduling(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        schedulingService.deleteScheduling(id);
    }

    @PutMapping("/{id}")
    public SchedulingResponse update(@PathVariable Long id, @RequestBody SchedulingRequest request) {
        return schedulingService.updateSchedulingTime(id, request);
    }
    @PatchMapping("/{id}")
    public SchedulingResponse markAsHappened(@PathVariable Long id) {
        return schedulingService.markAsHappened(id);
    }

}
