package com.example.nhatromanagement.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.YearMonth;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false)
    private int billMonth; // e.g., 5 for May

    @Column(nullable = false)
    private int billYear;  // e.g., 2025

    private double electricityKwhPrevious; // số điện tháng trước
    private double electricityKwhCurrent;  // số điện tháng này

    private double waterM3Previous;        // số nước tháng trước
    private double waterM3Current;         // số nước tháng này

    private double trashFee;               // tiền rác
    private double wifiFee;                // tiền wifi
    private double roomRent;               // tiền trọ cơ bản

    private double electricityCost;        // Calculated: (electricityKwhCurrent - electricityKwhPrevious) * 3000
    private double waterCost;              // Calculated: (waterM3Current - waterM3Previous) * 13000
    private double totalAmount;            // Calculated: roomRent + electricityCost + waterCost + trashFee + wifiFee

    private String occupantName;           // Tên người thuê tại thời điểm lập hóa đơn

    private Boolean paid = false;          // Trạng thái thanh toán

    // Method to calculate costs, prices are passed from a service
    public void calculateCosts(double electricityPricePerKwh, double waterPricePerM3) {
        if (electricityKwhCurrent >= electricityKwhPrevious) {
            this.electricityCost = (this.electricityKwhCurrent - this.electricityKwhPrevious) * electricityPricePerKwh;
        } else {
            this.electricityCost = 0; // Or handle error/edge case (e.g. meter reset)
        }

        if (waterM3Current >= waterM3Previous) {
            this.waterCost = (this.waterM3Current - this.waterM3Previous) * waterPricePerM3;
        } else {
            this.waterCost = 0; // Or handle error/edge case
        }

        this.totalAmount = this.roomRent + this.electricityCost + this.waterCost + this.trashFee + this.wifiFee;
    }

}
