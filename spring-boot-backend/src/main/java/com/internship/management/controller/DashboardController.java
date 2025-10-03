package com.internship.management.controller;

import com.internship.management.dto.ApiResponse;
import com.internship.management.dto.DashboardDTO;
import com.internship.management.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<DashboardDTO.DashboardMetrics>> getMetrics(
            @RequestParam Long userId
    ) {
        try {
            DashboardDTO.DashboardMetrics metrics = dashboardService.getMetrics(userId);
            return ResponseEntity.ok(ApiResponse.success(metrics));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/departments")
    public ResponseEntity<ApiResponse<List<DashboardDTO.DepartmentStats>>> getDepartmentStats() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getDepartmentStats()));
    }

    @GetMapping("/project-status")
    public ResponseEntity<ApiResponse<DashboardDTO.ProjectStatusStats>> getProjectStatusStats() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getProjectStatusStats()));
    }

    @GetMapping("/task-stats")
    public ResponseEntity<ApiResponse<DashboardDTO.TaskStats>> getTaskStats() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getTaskStats()));
    }
}
