package com.example.nhatromanagement.service;

import com.example.nhatromanagement.dto.BillStatisticsDTO;
import com.example.nhatromanagement.model.Bill;
import com.example.nhatromanagement.model.Tenant;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface BillService {
    Bill createBill(Long tenantId, int year, int month, double electricityKwhCurrent, double waterM3Current,
            double trashFee, double wifiFee, double roomRent, String occupantName);

    Optional<Bill> getBillById(Long id);

    List<Bill> getAllBills();

    List<Bill> getBillsByTenant(Long tenantId);

    Optional<Bill> getBillForTenantByMonthYear(Long tenantId, int year, int month);

    Bill updateBill(Bill bill); // A more generic update method

    void deleteBill(Long billId);

    Optional<Bill> getLatestBillForTenant(Long tenantId);

    /**
     * Get bill statistics by month and/or year
     * 
     * @param month Optional month filter (1-12), null for all months
     * @param year  Required year filter
     * @return BillStatisticsDTO with aggregated totals
     */
    BillStatisticsDTO getStatistics(Integer month, Integer year);

    /**
     * Get bill statistics by month, year and tenant
     * 
     * @param month    Optional month filter (1-12), null for all months
     * @param year     Required year filter
     * @param tenantId Optional tenant filter, null for all tenants
     * @return BillStatisticsDTO with aggregated totals
     */
    BillStatisticsDTO getStatistics(Integer month, Integer year, Long tenantId);

    /**
     * Get bills filtered by month and year
     */
    List<Bill> getBillsByMonthAndYear(int month, int year);

    /**
     * Get bills filtered by year only
     */
    List<Bill> getBillsByYear(int year);

    /**
     * Get bills filtered by tenant
     */
    List<Bill> getBillsByTenant(Tenant tenant);

    /**
     * Get bills filtered by tenant and year
     */
    List<Bill> getBillsByTenantAndYear(Tenant tenant, int year);

    /**
     * Get bills filtered by tenant, month and year
     */
    List<Bill> getBillsByTenantAndMonthYear(Tenant tenant, int month, int year);
}
