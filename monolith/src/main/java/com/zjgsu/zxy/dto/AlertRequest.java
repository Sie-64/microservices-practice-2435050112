package com.zjgsu.zxy.dto;
import lombok.Data;
@Data
public class AlertRequest {
    private Long vehicleId;
    private String alertType;
    private Integer level;
}