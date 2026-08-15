package com.petshop.api.report.controller;

import com.petshop.api.customer.domain.Customer;
import com.petshop.api.report.dto.AbsentCustomer;
import com.petshop.api.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping
    public String generateReport() {
        return reportService.generateReport();
    }

    @GetMapping("/absent")
    public List<AbsentCustomer> generated15Or30DaysOff() {
        return reportService.generated15Or30DaysOff();
    }
}