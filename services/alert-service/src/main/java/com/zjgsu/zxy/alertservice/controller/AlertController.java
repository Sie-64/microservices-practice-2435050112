package com.zjgsu.zxy.alertservice.controller;

import com.zjgsu.zxy.alertservice.dto.AlertRequest;
import com.zjgsu.zxy.alertservice.entity.Alert;
import com.zjgsu.zxy.alertservice.service.AlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {
    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @PostMapping
    public ResponseEntity<?> createAlert(@RequestBody AlertRequest req) {
        try {
            Alert a = new Alert();
            a.setVehicleId(req.getVehicleId());
            a.setAlertType(req.getAlertType());
            a.setLevel(req.getLevel());
            return ResponseEntity.ok(alertService.createAlert(a));
        } catch (IllegalArgumentException e) {
            // 校验失败返回 400
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // 服务调用失败返回 500
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<Alert>> getAlertsByVehicle(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(alertService.getAlertsByVehicle(vehicleId));
    }
}