package com.petshop.api.schedule.controller;

import com.petshop.api.schedule.dto.FutureScheduleRequest;
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
    @PatchMapping("/{id}/{status}")
    public SchedulingResponse changeStatus(@PathVariable Long id, @PathVariable String status) {
        return schedulingService.changeStatus(id, status);
    }

    @PostMapping("/future-schedules")
    @ResponseStatus(HttpStatus.CREATED)
    public List<SchedulingResponse> createFutureFromPack(@RequestBody FutureScheduleRequest request) {
        return schedulingService.createFutureFromPack(
                request.scheduling(),
                request.time(),
                request.packId()
        );
    }

    @PutMapping("/{id}/future-schedules")
    @ResponseStatus(HttpStatus.CREATED)
    public List<SchedulingResponse> updateFutureFromPack(
            @PathVariable Long id,
            @RequestBody FutureScheduleRequest request) {
        return schedulingService.updateFutureFromPack(
                id,
                request.scheduling()
        );
    }


}
