package com.example.nhatromanagement.service;

import com.example.nhatromanagement.model.Bill;
import com.example.nhatromanagement.model.Tenant;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface BillService {
    Bill createBill(Long tenantId, int year, int month, double electricityKwhCurrent, double waterM3Current, double trashFee, double wifiFee, double roomRent, String occupantName);
    Optional<Bill> getBillById(Long id);
    List<Bill> getAllBills();
    List<Bill> getBillsByTenant(Long tenantId);
    Optional<Bill> getBillForTenantByMonthYear(Long tenantId, int year, int month);
    Bill updateBill(Bill bill); // A more generic update method
    void deleteBill(Long billId);
    Optional<Bill> getLatestBillForTenant(Long tenantId);
}
