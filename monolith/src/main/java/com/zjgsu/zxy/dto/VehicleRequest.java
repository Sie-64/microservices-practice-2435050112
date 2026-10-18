package com.zjgsu.zxy.dto;

public class VehicleRequest {
    private String plateNumber;
    private String brand;
    private String tier;

    // Getters and Setters
    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }
}