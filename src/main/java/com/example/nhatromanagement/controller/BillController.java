package com.example.nhatromanagement.controller;

import com.example.nhatromanagement.model.Bill;
import com.example.nhatromanagement.model.Tenant;
import com.example.nhatromanagement.service.BillService;
import com.example.nhatromanagement.service.TenantService;
import com.example.nhatromanagement.service.SettingService; // Added SettingService import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.example.nhatromanagement.service.PdfService;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/bills")
public class BillController {

    private final BillService billService;
    private final TenantService tenantService;
    private final MessageSource messageSource;
    private final PdfService pdfService;
    private final SettingService settingService; // Added SettingService field

    @Autowired
    public BillController(BillService billService, TenantService tenantService, MessageSource messageSource,
            PdfService pdfService, SettingService settingService) { // Added SettingService to constructor
        this.billService = billService;
        this.tenantService = tenantService;
        this.messageSource = messageSource;
        this.pdfService = pdfService;
        this.settingService = settingService; // Initialize SettingService
    }

    @GetMapping
    public String listAllBills(
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "tenantId", required = false) Long tenantId,
            Model model) {

        List<Bill> bills;

        // Apply filters
        if (tenantId != null && month != null && year != null) {
            // Filter by tenant, month, and year
            Optional<Tenant> tenant = tenantService.getTenantById(tenantId);
            if (tenant.isPresent()) {
                bills = billService.getBillsByTenantAndMonthYear(tenant.get(), month, year);
            } else {
                bills = new java.util.ArrayList<>();
            }
        } else if (tenantId != null && year != null) {
            // Filter by tenant and year
            Optional<Tenant> tenant = tenantService.getTenantById(tenantId);
            if (tenant.isPresent()) {
                bills = billService.getBillsByTenantAndYear(tenant.get(), year);
            } else {
                bills = new java.util.ArrayList<>();
            }
        } else if (tenantId != null) {
            // Filter by tenant only
            Optional<Tenant> tenant = tenantService.getTenantById(tenantId);
            bills = tenant.map(t -> billService.getBillsByTenant(t)).orElse(new java.util.ArrayList<>());
        } else if (month != null && year != null) {
            // Filter by month and year
            bills = billService.getBillsByMonthAndYear(month, year);
        } else if (year != null) {
            // Filter by year only
            bills = billService.getBillsByYear(year);
        } else {
            // No filter - get all bills
            bills = billService.getAllBills();
        }

        model.addAttribute("bills", bills);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedTenantId", tenantId);

        // Get unique years from all bills for year dropdown
        List<Bill> allBills = billService.getAllBills();
        java.util.Set<Integer> yearsSet = new java.util.TreeSet<>(java.util.Collections.reverseOrder());
        for (Bill b : allBills) {
            yearsSet.add(b.getBillYear());
        }
        model.addAttribute("years", new java.util.ArrayList<>(yearsSet));

        // Get all tenants for dropdown
        model.addAttribute("tenants", tenantService.getAllTenants());

        return "bills/list";
    }

    @GetMapping("/tenant/{tenantId}")
    public String listBillsByTenant(@PathVariable("tenantId") Long tenantId, Model model,
            RedirectAttributes redirectAttributes) {
        Optional<Tenant> tenantOptional = tenantService.getTenantById(tenantId);
        if (tenantOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tenant not found."); // Consider using message key
            return "redirect:/tenants";
        }
        model.addAttribute("tenant", tenantOptional.get());
        model.addAttribute("bills", tenantOptional.get().getBills());
        return "bills/list-by-tenant";
    }

    @GetMapping("/add/{tenantId}")
    public String showAddBillForm(@PathVariable("tenantId") Long tenantId, Model model,
            RedirectAttributes redirectAttributes) {
        Optional<Tenant> tenantOptional = tenantService.getTenantById(tenantId);
        if (tenantOptional.isEmpty()) {
            String errorMsg = messageSource.getMessage("error.tenant.notfound", new Object[] { tenantId },
                    LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            return "redirect:/tenants";
        }

        Tenant tenant = tenantOptional.get();
        Bill bill = new Bill();
        bill.setTenant(tenant);

        LocalDate today = LocalDate.now();
        bill.setBillMonth(today.getMonthValue());
        bill.setBillYear(today.getYear());

        Optional<Bill> latestBill = billService.getLatestBillForTenant(tenantId);
        String prefilledOccupantName = tenant.getName(); // Default to room name
        if (latestBill.isPresent()) {
            Bill lb = latestBill.get();
            bill.setElectricityKwhPrevious(lb.getElectricityKwhCurrent());
            bill.setWaterM3Previous(lb.getWaterM3Current());
            if (lb.getOccupantName() != null && !lb.getOccupantName().trim().isEmpty()) {
                prefilledOccupantName = lb.getOccupantName();
            }
            // Auto-load fixed fees from previous bill
            bill.setRoomRent(lb.getRoomRent());
            bill.setTrashFee(lb.getTrashFee());
            bill.setWifiFee(lb.getWifiFee());
        }
        bill.setOccupantName(prefilledOccupantName);

        model.addAttribute("bill", bill);
        model.addAttribute("tenantName", tenant.getName());
        model.addAttribute("isEditMode", false);
        model.addAttribute("pageTitle", messageSource.getMessage("bill.add.title", new Object[] { tenant.getName() },
                LocaleContextHolder.getLocale()));
        return "bills/form";
    }

    @GetMapping("/edit/{id}")
    public String showEditBillForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Bill> billOptional = billService.getBillById(id);
        if (billOptional.isEmpty()) {
            String errorMsg = messageSource.getMessage("error.bill.notfound", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            return "redirect:/bills";
        }
        Bill bill = billOptional.get();
        model.addAttribute("bill", bill);
        if (bill.getTenant() != null) {
            model.addAttribute("tenantName", bill.getTenant().getName()); // tenantName is used by the form for context
            model.addAttribute("isEditMode", true);
            model.addAttribute("pageTitle", messageSource.getMessage("bill.edit.title",
                    new Object[] { bill.getTenant().getName(), bill.getId() }, LocaleContextHolder.getLocale()));
        } else {
            // This case should ideally not happen if data integrity is maintained
            String errorMsg = messageSource.getMessage("error.bill.tenant.missing", null,
                    LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            return "redirect:/bills"; // Or a more specific error page
        }
        return "bills/form";
    }

    @PostMapping("/save")
    public String saveBill(@ModelAttribute("bill") Bill bill,
            @RequestParam(value = "occupantName", required = false) String occupantNameFromForm,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        bill.setOccupantName(occupantNameFromForm);

        // Ensure tenant information is present
        if (bill.getTenant() == null || bill.getTenant().getId() == null) {
            String errorMsg = messageSource.getMessage("error.bill.tenant.required", null,
                    LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            // Smart redirect based on context
            if (bill.getId() != null) { // Editing an existing bill
                return "redirect:/bills/edit/" + bill.getId();
            }
            // If adding and tenant info lost, redirect to tenant list or a general error
            // page
            return "redirect:/tenants";
        }

        if (result.hasErrors()) {
            // Repopulate tenantName for the form
            tenantService.getTenantById(bill.getTenant().getId())
                    .ifPresent(t -> redirectAttributes.addFlashAttribute("tenantName", t.getName()));

            String redirectPath;
            if (bill.getId() != null) { // Editing existing bill
                redirectPath = "redirect:/bills/edit/" + bill.getId();
            } else { // Adding new bill
                redirectPath = "redirect:/bills/add/" + bill.getTenant().getId();
            }
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.bill", result);
            redirectAttributes.addFlashAttribute("bill", bill);
            return redirectPath;
        }

        try {
            String successMsgKey;
            if (bill.getId() != null) { // Update existing bill
                billService.updateBill(bill); // Assumes updateBill takes the Bill object
                successMsgKey = "success.bill.updated";
            } else { // Create new bill
                billService.createBill(
                        bill.getTenant().getId(),
                        bill.getBillYear(),
                        bill.getBillMonth(),
                        bill.getElectricityKwhCurrent(),
                        bill.getWaterM3Current(),
                        bill.getTrashFee(),
                        bill.getWifiFee(),
                        bill.getRoomRent(),
                        bill.getOccupantName());
                successMsgKey = "success.bill.saved"; // This was bill.create.success, changing to success.bill.saved
                                                      // for consistency
            }
            String successMsg = messageSource.getMessage(successMsgKey, null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", successMsg);
            return "redirect:/bills/tenant/" + bill.getTenant().getId();
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            // Repopulate tenantName for the form
            tenantService.getTenantById(bill.getTenant().getId())
                    .ifPresent(t -> redirectAttributes.addFlashAttribute("tenantName", t.getName()));

            String errorRedirectPath;
            if (bill.getId() != null) { // Editing existing bill
                errorRedirectPath = "redirect:/bills/edit/" + bill.getId();
            } else { // Adding new bill
                errorRedirectPath = "redirect:/bills/add/" + bill.getTenant().getId();
            }
            redirectAttributes.addFlashAttribute("bill", bill);
            return errorRedirectPath;
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteBill(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Optional<Bill> billOptional = billService.getBillById(id);
        if (billOptional.isEmpty()) {
            String errorMsg = messageSource.getMessage("error.bill.notfound", new Object[] { id },
                    LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            return "redirect:/bills/all"; // Or a more appropriate error page/redirect
        }

        Long tenantId = billOptional.get().getTenant().getId();

        try {
            billService.deleteBill(id);
            String successMsg = messageSource.getMessage("success.bill.deleted", new Object[] { id },
                    LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", successMsg);
        } catch (IllegalArgumentException e) {
            // This might happen if the bill is already deleted by another process, though
            // existsById check in service should prevent most.
            String errorMsg = messageSource.getMessage("error.bill.delete.failed", new Object[] { id },
                    LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg + ": " + e.getMessage());
        } catch (Exception e) {
            // Catch any other unexpected errors during deletion
            String errorMsg = messageSource.getMessage("error.bill.delete.failed", new Object[] { id },
                    LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg + ": " + e.getMessage());
        }
        return "redirect:/bills/tenant/" + tenantId;
    }

    @GetMapping("/{id}")
    public String viewBill(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Bill> billOptional = billService.getBillById(id);
        if (billOptional.isEmpty()) {
            String errorMsg = messageSource.getMessage("error.bill.notfound", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            return "redirect:/bills";
        }
        Bill bill = billOptional.get();
        model.addAttribute("bill", bill);

        // Fetch and add dynamic prices to the model
        double electricityPriceUnit = settingService.getDoubleSettingValue("ELECTRICITY_PRICE").orElse(0.0); // Default
                                                                                                             // if not
                                                                                                             // set
        double waterPriceUnit = settingService.getDoubleSettingValue("WATER_PRICE").orElse(0.0); // Default if not set
        model.addAttribute("electricityPriceUnit", electricityPriceUnit);
        model.addAttribute("waterPriceUnit", waterPriceUnit);

        model.addAttribute("pageTitle",
                messageSource.getMessage("bill.details.title",
                        new Object[] { bill.getTenant().getName(), bill.getBillMonth(), bill.getBillYear() },
                        LocaleContextHolder.getLocale()));
        return "bills/detail";
    }

    @GetMapping("/export/pdf/{billId}")
    public ResponseEntity<byte[]> exportBillToPdf(@PathVariable("billId") Long billId,
            RedirectAttributes redirectAttributes) {
        Optional<Bill> billOptional = billService.getBillById(billId);
        if (billOptional.isEmpty()) {
            // This path won't be hit if called from a valid bill's detail page usually,
            // but good for direct URL access attempts.
            // Consider how to communicate this error - redirectAttributes won't work with
            // ResponseEntity.
            // For simplicity, returning NOT_FOUND. A proper error page or JSON response
            // might be better.
            return ResponseEntity.notFound().build();
        }

        Bill bill = billOptional.get();
        try {
            ByteArrayOutputStream pdfOutputStream = pdfService.generateBillPdf(bill);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = String.format("bill_%s_%d_%d.pdf", bill.getTenant().getName().replaceAll("\\s+", "_"),
                    bill.getBillMonth(), bill.getBillYear());
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfOutputStream.toByteArray(), headers, HttpStatus.OK);
        } catch (Exception e) {
            // Log the error e.printStackTrace(); or use a logger
            // Redirecting with error message is tricky here. Returning an internal server
            // error.
            // A user-friendly error page or a JSON error response would be better for
            // production.
            System.err.println("Error generating PDF for bill ID " + billId + ": " + e.getMessage());
            e.printStackTrace();
            // Optionally, redirect to an error page or back to bill details with a flash
            // message if possible,
            // but ResponseEntity makes direct redirection with flash attributes hard.
            // For now, send an error status.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Or a generic error message in
                                                                                       // bytes
        }
    }

    @GetMapping("/statistics")
    public String showStatistics(
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "tenantId", required = false) Long tenantId,
            Model model) {

        // Default to current year if no year specified
        if (year == null) {
            year = java.time.LocalDate.now().getYear();
        }

        com.example.nhatromanagement.dto.BillStatisticsDTO statistics = billService.getStatistics(month, year,
                tenantId);
        model.addAttribute("statistics", statistics);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedTenantId", tenantId);

        // Generate list of years (current year and 5 years back)
        int currentYear = java.time.LocalDate.now().getYear();
        java.util.List<Integer> years = new java.util.ArrayList<>();
        for (int i = currentYear; i >= currentYear - 5; i--) {
            years.add(i);
        }
        model.addAttribute("years", years);

        // Get all tenants for dropdown
        model.addAttribute("tenants", tenantService.getAllTenants());

        // Get selected tenant name for display
        if (tenantId != null) {
            tenantService.getTenantById(tenantId).ifPresent(t -> model.addAttribute("selectedTenantName", t.getName()));
        }

        model.addAttribute("pageTitle",
                messageSource.getMessage("statistics.title", null, LocaleContextHolder.getLocale()));
        return "bills/statistics";
    }
}
