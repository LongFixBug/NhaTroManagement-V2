package com.example.nhatromanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for quick meter reading entry per room
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeterReadingDTO {

    private Long tenantId;
    private String tenantName;

    // Previous readings (from last bill, read-only display)
    private double electricityPrevious;
    private double waterPrevious;

    // Current readings (user input)
    private Double electricityCurrent;
    private Double waterCurrent;

    // Pre-filled fees from last bill
    private double roomRent;
    private double trashFee;
    private double wifiFee;

    // Occupant name
    private String occupantName;

    // Whether to create bill for this room
    private boolean selected = true;
}
