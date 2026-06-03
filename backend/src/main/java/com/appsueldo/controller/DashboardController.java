package com.appsueldo.controller;

import com.appsueldo.dto.MonthlySummaryDto;
import com.appsueldo.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/monthly-summary")
    public MonthlySummaryDto monthlySummary() {
        return dashboardService.monthlySummary();
    }
}
