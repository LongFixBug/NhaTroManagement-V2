package com.example.nhatromanagement.controller;

import com.example.nhatromanagement.dto.BulkMeterReadingDTO;
import com.example.nhatromanagement.dto.MeterReadingDTO;
import com.example.nhatromanagement.model.Bill;
import com.example.nhatromanagement.model.Tenant;
import com.example.nhatromanagement.service.BillService;
import com.example.nhatromanagement.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/quick-entry")
public class QuickEntryController {

    private final TenantService tenantService;
    private final BillService billService;
    private final MessageSource messageSource;

    @Autowired
    public QuickEntryController(TenantService tenantService, BillService billService,
            MessageSource messageSource) {
        this.tenantService = tenantService;
        this.billService = billService;
        this.messageSource = messageSource;
    }

    @GetMapping
    public String showQuickEntryForm(Model model) {
        List<Tenant> tenants = tenantService.getAllTenants();

        BulkMeterReadingDTO bulkDTO = new BulkMeterReadingDTO();

        // Find the latest bill month/year in database and default to next month
        int defaultMonth = LocalDate.now().getMonthValue();
        int defaultYear = LocalDate.now().getYear();

        // Check if any bills exist in database to auto-increment month
        List<Bill> allBills = billService.getAllBills();
        if (!allBills.isEmpty()) {
            // Find the latest bill by year and month
            Bill latestBill = allBills.stream()
                    .max((a, b) -> {
                        int yearCompare = Integer.compare(a.getBillYear(), b.getBillYear());
                        if (yearCompare != 0)
                            return yearCompare;
                        return Integer.compare(a.getBillMonth(), b.getBillMonth());
                    })
                    .orElse(null);

            if (latestBill != null) {
                // Set to next month after the latest bill
                defaultMonth = latestBill.getBillMonth() + 1;
                defaultYear = latestBill.getBillYear();

                // Handle year rollover (December -> January)
                if (defaultMonth > 12) {
                    defaultMonth = 1;
                    defaultYear++;
                }
            }
        }

        bulkDTO.setBillMonth(defaultMonth);
        bulkDTO.setBillYear(defaultYear);

        List<MeterReadingDTO> readings = new ArrayList<>();

        for (Tenant tenant : tenants) {
            MeterReadingDTO dto = new MeterReadingDTO();
            dto.setTenantId(tenant.getId());
            dto.setTenantName(tenant.getName());

            // Get latest bill for previous readings and prefilled fees
            Optional<Bill> latestBill = billService.getLatestBillForTenant(tenant.getId());
            if (latestBill.isPresent()) {
                Bill lb = latestBill.get();
                dto.setElectricityPrevious(lb.getElectricityKwhCurrent());
                dto.setWaterPrevious(lb.getWaterM3Current());
                dto.setRoomRent(lb.getRoomRent());
                dto.setTrashFee(lb.getTrashFee());
                dto.setWifiFee(lb.getWifiFee());
                dto.setOccupantName(lb.getOccupantName());
            } else {
                dto.setElectricityPrevious(0);
                dto.setWaterPrevious(0);
                dto.setRoomRent(0);
                dto.setTrashFee(0);
                dto.setWifiFee(0);
                dto.setOccupantName(tenant.getName());
            }

            dto.setSelected(true);
            readings.add(dto);
        }

        bulkDTO.setReadings(readings);
        model.addAttribute("bulkDTO", bulkDTO);
        model.addAttribute("pageTitle",
                messageSource.getMessage("quickEntry.title", null, LocaleContextHolder.getLocale()));

        return "bills/quick-entry";
    }

    @PostMapping("/save")
    public String saveBulkBills(@ModelAttribute BulkMeterReadingDTO bulkDTO, RedirectAttributes redirectAttributes) {
        int successCount = 0;
        int errorCount = 0;
        List<String> errors = new ArrayList<>();

        for (MeterReadingDTO reading : bulkDTO.getReadings()) {
            if (!reading.isSelected()) {
                continue; // Skip unselected rooms
            }

            if (reading.getElectricityCurrent() == null || reading.getWaterCurrent() == null) {
                continue; // Skip if no readings entered
            }

            try {
                billService.createBill(
                        reading.getTenantId(),
                        bulkDTO.getBillYear(),
                        bulkDTO.getBillMonth(),
                        reading.getElectricityCurrent(),
                        reading.getWaterCurrent(),
                        reading.getTrashFee(),
                        reading.getWifiFee(),
                        reading.getRoomRent(),
                        reading.getOccupantName());
                successCount++;
            } catch (IllegalStateException e) {
                // Bill already exists for this month
                errors.add(reading.getTenantName() + ": " + e.getMessage());
                errorCount++;
            } catch (Exception e) {
                errors.add(reading.getTenantName() + ": " + e.getMessage());
                errorCount++;
            }
        }

        if (successCount > 0) {
            String successMsg = messageSource.getMessage("quickEntry.success",
                    new Object[] { successCount }, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", successMsg);
        }

        if (errorCount > 0) {
            String errorMsg = messageSource.getMessage("quickEntry.errors",
                    new Object[] { errorCount, String.join("; ", errors) }, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
        }

        return "redirect:/bills";
    }
}
