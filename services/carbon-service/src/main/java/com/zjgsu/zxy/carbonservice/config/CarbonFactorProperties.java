package com.zjgsu.zxy.carbonservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "carbon.factors")
public class CarbonFactorProperties {

    private BigDecimal gasolineKgPerLiter = new BigDecimal("2.31");
    private BigDecimal gridKgPerKwh = new BigDecimal("0.581");

    public BigDecimal getGasolineKgPerLiter() {
        return gasolineKgPerLiter;
    }

    public void setGasolineKgPerLiter(BigDecimal gasolineKgPerLiter) {
        this.gasolineKgPerLiter = gasolineKgPerLiter;
    }

    public BigDecimal getGridKgPerKwh() {
        return gridKgPerKwh;
    }

    public void setGridKgPerKwh(BigDecimal gridKgPerKwh) {
        this.gridKgPerKwh = gridKgPerKwh;
    }
}
