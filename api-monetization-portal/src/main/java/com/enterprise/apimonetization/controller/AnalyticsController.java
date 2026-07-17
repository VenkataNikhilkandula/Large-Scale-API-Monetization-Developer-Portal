package com.enterprise.apimonetization.controller;

import com.enterprise.apimonetization.dto.AnalyticsDashboardResponse;
import com.enterprise.apimonetization.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Analytics & Dashboard", description = "Endpoints for usage metrics dashboards")
@SecurityRequirement(name = "BearerAuth")
@PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get aggregated analytics metrics (total APIs, developers, monthly requests, requests per API)")
    public ResponseEntity<AnalyticsDashboardResponse> getDashboardStats() {
        return ResponseEntity.ok(analyticsService.getDashboardStats());
    }
}
