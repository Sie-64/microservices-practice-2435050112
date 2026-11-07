package com.zjgsu.zxy.carbonservice.controller;

import com.zjgsu.zxy.carbonservice.dto.CarbonReportRequest;
import com.zjgsu.zxy.carbonservice.dto.CarbonReportResponse;
import com.zjgsu.zxy.carbonservice.service.CarbonReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/carbon/reports")
public class CarbonReportController {

    private final CarbonReportService service;

    public CarbonReportController(CarbonReportService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CarbonReportResponse> create(
            @RequestBody CarbonReportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.generateReport(request));
    }

    @GetMapping("/vehicle/{vehicleId}")
    public List<CarbonReportResponse> getReports(@PathVariable Long vehicleId) {
        return service.getReports(vehicleId);
    }

    @GetMapping("/vehicle/{vehicleId}/monthly")
    public CarbonReportResponse getReport(
            @PathVariable Long vehicleId,
            @RequestParam String month) {
        return service.getReport(vehicleId, month);
    }
}
