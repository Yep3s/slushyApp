package com.example.SlushyApp.Controller;

import com.example.SlushyApp.DTO.DashboardStatsDto;
import com.example.SlushyApp.Service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {

    private final DashboardService dashboardService;
    public AdminDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public DashboardStatsDto statsHoy() {
        return dashboardService.calcularStatsDeHoy();
    }

}
