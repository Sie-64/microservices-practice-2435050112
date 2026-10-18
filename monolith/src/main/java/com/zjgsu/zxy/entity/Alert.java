package com.zjgsu.zxy.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "alerts")
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long vehicleId; // 关联车辆

    @Column(nullable = false)
    private String alertType; // 告警类型，如"漏电"、"老化"

    @Column(nullable = false)
    private Integer level; // 告警等级：1, 2, 3

    private String status; // 状态: pending/handled
}