package com.example.nhatromanagement.service.impl;

import com.example.nhatromanagement.model.Bill;
import com.example.nhatromanagement.model.Tenant;
import com.example.nhatromanagement.repository.BillRepository;
import com.example.nhatromanagement.repository.TenantRepository;
import com.example.nhatromanagement.service.BillService;
import com.example.nhatromanagement.service.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
public class BillServiceImpl implements BillService {

    private final BillRepository billRepository;
    private final TenantRepository tenantRepository;
    private final SettingService settingService;

    @Autowired
    public BillServiceImpl(BillRepository billRepository, TenantRepository tenantRepository, SettingService settingService) {
        this.billRepository = billRepository;
        this.tenantRepository = tenantRepository;
        this.settingService = settingService;
    }

    @Override
    @Transactional
    public Bill createBill(Long tenantId, int year, int month, double electricityKwhCurrent, double waterM3Current, double trashFee, double wifiFee, double roomRent, String occupantName) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found with id: " + tenantId));

        // Check if a bill for this tenant, month, and year already exists
        Optional<Bill> existingBill = billRepository.findByTenantAndBillMonthAndBillYear(tenant, month, year);
        if (existingBill.isPresent()) {
            throw new IllegalStateException("A bill already exists for tenant " + tenantId + " for " + month + "/" + year);
        }

        Bill newBill = new Bill();
        newBill.setTenant(tenant);
        newBill.setBillYear(year);
        newBill.setBillMonth(month);

        // Get previous month's readings
        Optional<Bill> previousMonthBill = getLatestBillForTenantBefore(tenant, year, month);

        newBill.setElectricityKwhPrevious(previousMonthBill.map(Bill::getElectricityKwhCurrent).orElse(0.0));
        newBill.setElectricityKwhCurrent(electricityKwhCurrent);
        newBill.setWaterM3Previous(previousMonthBill.map(Bill::getWaterM3Current).orElse(0.0));
        newBill.setWaterM3Current(waterM3Current);

        newBill.setTrashFee(trashFee);
        newBill.setWifiFee(wifiFee);
        newBill.setRoomRent(roomRent);
        newBill.setOccupantName(occupantName);

        // Fetch dynamic prices
        double electricityPrice = settingService.getDoubleSettingValue("ELECTRICITY_PRICE").orElse(0.0); // Default to 0.0 if not set
        double waterPrice = settingService.getDoubleSettingValue("WATER_PRICE").orElse(0.0); // Default to 0.0 if not set

        newBill.calculateCosts(electricityPrice, waterPrice);
        return billRepository.save(newBill);
    }

    private Optional<Bill> getLatestBillForTenantBefore(Tenant tenant, int year, int month) {
        YearMonth currentBillingPeriod = YearMonth.of(year, month);
        YearMonth previousBillingPeriod = currentBillingPeriod.minusMonths(1);
        return billRepository.findByTenantAndBillMonthAndBillYear(tenant, previousBillingPeriod.getMonthValue(), previousBillingPeriod.getYear());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Bill> getBillById(Long id) {
        return billRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Bill> getAllBills() {
        return billRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Bill> getBillsByTenant(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found with id: " + tenantId));
        // This might be inefficient if a tenant has many bills. Consider pagination or specific queries.
        // For now, let's assume it's fine. A dedicated repository method might be better.
        return tenant.getBills(); // Assuming Tenant entity has a getBills() method from a @OneToMany mapping
                                  // If not, we need to query BillRepository by tenant
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Bill> getBillForTenantByMonthYear(Long tenantId, int year, int month) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found with id: " + tenantId));
        return billRepository.findByTenantAndBillMonthAndBillYear(tenant, month, year);
    }

    @Override
    @Transactional
    public Bill updateBill(Bill billFromForm) { // Renamed parameter for clarity
        if (billFromForm.getId() == null) {
            throw new IllegalArgumentException("Bill ID cannot be null for an update.");
        }

        Bill existingBill = billRepository.findById(billFromForm.getId())
                .orElseThrow(() -> new IllegalArgumentException("Bill not found with id: " + billFromForm.getId()));

        // Update editable fields from the form data
        existingBill.setBillYear(billFromForm.getBillYear());
        existingBill.setBillMonth(billFromForm.getBillMonth());
        existingBill.setElectricityKwhCurrent(billFromForm.getElectricityKwhCurrent());
        existingBill.setWaterM3Current(billFromForm.getWaterM3Current());
        existingBill.setTrashFee(billFromForm.getTrashFee());
        existingBill.setWifiFee(billFromForm.getWifiFee());
        existingBill.setRoomRent(billFromForm.getRoomRent());
        existingBill.setOccupantName(billFromForm.getOccupantName());
        existingBill.setPaid(billFromForm.getPaid());

        // electricityKwhPrevious and waterM3Previous are typically not changed during an edit
        // as they are historical values from the point of creation or the actual previous bill.

        // The tenant association should also not change during a bill edit.

        // Fetch dynamic prices
        double electricityPrice = settingService.getDoubleSettingValue("ELECTRICITY_PRICE").orElse(0.0); // Default to 0.0 if not set
        double waterPrice = settingService.getDoubleSettingValue("WATER_PRICE").orElse(0.0); // Default to 0.0 if not set

        existingBill.calculateCosts(electricityPrice, waterPrice); // Recalculate based on potentially new current readings and fees
        return billRepository.save(existingBill);
    }

    @Override
    @Transactional
    public void deleteBill(Long id) {
        if (!billRepository.existsById(id)) {
            throw new IllegalArgumentException("Bill not found with id: " + id);
        }
        billRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Bill> getLatestBillForTenant(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid tenant ID: " + tenantId));
        return billRepository.findTopByTenantOrderByBillYearDescBillMonthDescIdDesc(tenant);
    }
}
