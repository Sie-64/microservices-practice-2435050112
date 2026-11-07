package com.zjgsu.zxy.carbonservice;

import com.zjgsu.zxy.carbonservice.config.CarbonFactorProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(CarbonFactorProperties.class)
public class CarbonServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarbonServiceApplication.class, args);
    }
}
