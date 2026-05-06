package com.petshop.api.schedule.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petshop.api.auth.filter.JwtFilter;
import com.petshop.api.schedule.dto.SchedulingRequest;
import com.petshop.api.schedule.dto.SchedulingResponse;
import com.petshop.api.schedule.service.SchedulingService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SchedulingController.class)
@AutoConfigureMockMvc(addFilters = false)

class SchedulingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SchedulingService schedulingService;

    @MockitoBean
    private JwtFilter jwtFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private SchedulingResponse buildResponse() {
        return new SchedulingResponse(
                1L, 1L, "Pedro", 1L, "Nox",
                "observação teste",
                LocalDateTime.of(2026, 5, 10, 14, 0),
                false, false, 60, false, List.of()
        );
    }

    private SchedulingRequest buildRequest() {
        return new SchedulingRequest(
                1L, "Pedro", 1L, "Nox",
                "observação teste",
                LocalDateTime.parse("2026-04-20T14:30:00"),
                false, 60, List.of()
        );
    }

    @Test
    void shouldCreateScheduling() throws Exception {
        when(schedulingService.createScheduling(any())).thenReturn(buildResponse());

        mockMvc.perform(post("/api/scheduling")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest()))
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerName").value("Pedro"))
                .andExpect(jsonPath("$.petName").value("Nox"));
    }

    @Test
    void shouldListSchedulings() throws Exception {
        when(schedulingService.listScheduling()).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/scheduling")
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].customerName").value("Pedro"))
                .andExpect(jsonPath("$[0].scheduleHappened").value(false));
    }

    @Test
    void shouldGetSchedulingById() throws Exception {
        when(schedulingService.getScheduling(1L)).thenReturn(buildResponse());

        mockMvc.perform(get("/api/scheduling/1")
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.petName").value("Nox"));
    }

    @Test
    void shouldReturn404WhenSchedulingNotFound() throws Exception {
        when(schedulingService.getScheduling(99L))
                .thenThrow(new EntityNotFoundException("Scheduling not found"));

        mockMvc.perform(get("/api/scheduling/99")
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteScheduling() throws Exception {
        doNothing().when(schedulingService).deleteScheduling(1L);

        mockMvc.perform(delete("/api/scheduling/1")
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentScheduling() throws Exception {
        doThrow(new EntityNotFoundException("Scheduling not found"))
                .when(schedulingService).deleteScheduling(99L);

        mockMvc.perform(delete("/api/scheduling/99")
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateScheduling() throws Exception {
        var updated = new SchedulingResponse(
                1L, 1L, "Pedro", 1L, "Nox",
                "observação atualizada",
                LocalDateTime.of(2026, 5, 15, 10, 0),
                false, false, 60, false, List.of()
        );

        when(schedulingService.updateSchedulingTime(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/api/scheduling/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest()))
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schedulingObservations").value("observação atualizada"));
    }

    @Test
    void shouldMarkSchedulingAsHappened() throws Exception {
        var happened = new SchedulingResponse(
                1L, 1L, "Pedro", 1L, "Nox",
                "observação teste",
                LocalDateTime.of(2026, 5, 10, 14, 0),
                true, false, 60, false, List.of()
        );

        when(schedulingService.markAsHappened(1L)).thenReturn(happened);

        mockMvc.perform(patch("/api/scheduling/1")
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scheduleHappened").value(true));
    }
}