package com.zjgsu.zxy;

import com.zjgsu.zxy.entity.Alert;
import com.zjgsu.zxy.service.AlertService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BusinessIntegrationTest {

    @Autowired
    private AlertService alertService;

    @Test
    void testAlertLevelBusinessRule() {
        Alert validAlert = new Alert();
        validAlert.setVehicleId(1L);
        validAlert.setAlertType("漏电");
        validAlert.setLevel(1);

        Alert saved = alertService.createAlert(validAlert);
        assertNotNull(saved.getId());
        assertEquals("pending", saved.getStatus());

        Alert invalidAlert = new Alert();
        invalidAlert.setVehicleId(1L);
        invalidAlert.setAlertType("老化");
        invalidAlert.setLevel(5); // 非法等级

        assertThrows(IllegalArgumentException.class, () -> {
            alertService.createAlert(invalidAlert);
        });
    }
}