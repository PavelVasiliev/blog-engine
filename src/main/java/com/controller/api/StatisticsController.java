package com.controller.api;

import com.api.response.StatisticsResponse;
import com.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Autowired
    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('user:write')")
    public StatisticsResponse statisticsMy() {
        return statisticsService.getMyStats();
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<StatisticsResponse> statisticsAll() {
        return statisticsService.getAllStats();
    }
}
