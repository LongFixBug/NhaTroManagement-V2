package com.example.nhatromanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for monthly statistics data (for chart display)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyStatDTO {

    private int month;
    private int year;
    private double totalRoomRent;
    private double totalElectricityCost;
    private double totalWaterCost;
    private double totalAmount;
    private int billCount;

    public String getMonthLabel() {
        return month + "/" + year;
    }
}
