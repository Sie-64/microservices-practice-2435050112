package com.zjgsu.zxy.drivingbehavior.controller;

import com.zjgsu.zxy.drivingbehavior.dto.DrivingEventRequest;
import com.zjgsu.zxy.drivingbehavior.dto.DrivingReportResponse;
import com.zjgsu.zxy.drivingbehavior.entity.DrivingEvent;
import com.zjgsu.zxy.drivingbehavior.service.DrivingBehaviorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api")
public class DrivingBehaviorController {

    private final DrivingBehaviorService service;

    public DrivingBehaviorController(DrivingBehaviorService service) {
        this.service = service;
    }

    @PostMapping("/driving-events")
    public ResponseEntity<DrivingEvent> createEvent(
            @RequestBody DrivingEventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createEvent(request));
    }

    @GetMapping("/driving-events/vehicle/{vehicleId}")
    public List<DrivingEvent> getEvents(@PathVariable Long vehicleId) {
        return service.getEvents(vehicleId);
    }

    @GetMapping("/driving-reports/vehicle/{vehicleId}/monthly")
    public DrivingReportResponse getMonthlyReport(
            @PathVariable Long vehicleId,
            @RequestParam(required = false) String month) {
        String reportMonth = month == null ? YearMonth.now().toString() : month;
        return service.buildMonthlyReport(vehicleId, reportMonth);
    }
}
