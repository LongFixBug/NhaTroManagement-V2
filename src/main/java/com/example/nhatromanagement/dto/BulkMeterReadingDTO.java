package com.example.nhatromanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper DTO for bulk meter reading form submission
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkMeterReadingDTO {

    private int billMonth;
    private int billYear;

    private List<MeterReadingDTO> readings = new ArrayList<>();
}
