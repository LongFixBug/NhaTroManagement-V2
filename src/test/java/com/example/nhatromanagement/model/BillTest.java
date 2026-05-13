package com.example.nhatromanagement.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BillTest {

    @Test
    void calculateCosts_normalCase() {
        Bill bill = new Bill();
        bill.setElectricityKwhPrevious(100);
        bill.setElectricityKwhCurrent(150);
        bill.setWaterM3Previous(10);
        bill.setWaterM3Current(20);
        bill.setRoomRent(3000000);
        bill.setTrashFee(20000);
        bill.setWifiFee(50000);

        bill.calculateCosts(3000, 13000);

        assertEquals(150000, bill.getElectricityCost()); // (150-100)*3000
        assertEquals(130000, bill.getWaterCost());       // (20-10)*13000
        assertEquals(3350000, bill.getTotalAmount());     // 3000000+150000+130000+20000+50000
    }

    @Test
    void calculateCosts_electricityCurrentLessThanPrevious_throws() {
        Bill bill = new Bill();
        bill.setElectricityKwhPrevious(200);
        bill.setElectricityKwhCurrent(100);
        bill.setWaterM3Previous(10);
        bill.setWaterM3Current(20);
        bill.setRoomRent(3000000);
        bill.setTrashFee(20000);
        bill.setWifiFee(50000);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> bill.calculateCosts(3000, 13000));
        assertTrue(ex.getMessage().contains("không thể nhỏ hơn chỉ số cũ"));
    }

    @Test
    void calculateCosts_waterCurrentLessThanPrevious_throws() {
        Bill bill = new Bill();
        bill.setElectricityKwhPrevious(100);
        bill.setElectricityKwhCurrent(150);
        bill.setWaterM3Previous(50);
        bill.setWaterM3Current(30);
        bill.setRoomRent(3000000);
        bill.setTrashFee(20000);
        bill.setWifiFee(50000);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> bill.calculateCosts(3000, 13000));
        assertTrue(ex.getMessage().contains("nước"));
    }

    @Test
    void calculateCosts_equalValues_works() {
        Bill bill = new Bill();
        bill.setElectricityKwhPrevious(100);
        bill.setElectricityKwhCurrent(100);
        bill.setWaterM3Previous(10);
        bill.setWaterM3Current(10);
        bill.setRoomRent(3000000);
        bill.setTrashFee(20000);
        bill.setWifiFee(50000);

        bill.calculateCosts(3000, 13000);

        assertEquals(0, bill.getElectricityCost());
        assertEquals(0, bill.getWaterCost());
    }
}
