package com.zjgsu.zxy.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "vehicles")
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String plateNumber; // 车牌号

    private String brand; // 品牌

    @Column(nullable = false)
    private String tier; // 套餐等级: light/common/full
}