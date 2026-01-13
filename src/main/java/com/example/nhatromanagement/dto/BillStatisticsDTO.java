package com.example.nhatromanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for bill statistics aggregation by month/year
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillStatisticsDTO {

    private Integer month;
    private Integer year;

    private double totalRoomRent;
    private double totalElectricityCost;
    private double totalWaterCost;
    private double totalTrashFee;
    private double totalWifiFee;
    private double totalAmount;

    private int billCount;

    /**
     * Calculate grand total from all components
     */
    public double getGrandTotal() {
        return totalRoomRent + totalElectricityCost + totalWaterCost + totalTrashFee + totalWifiFee;
    }
}
