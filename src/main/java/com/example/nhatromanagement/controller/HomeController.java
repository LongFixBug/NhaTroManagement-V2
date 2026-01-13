package com.example.nhatromanagement.controller;

import com.example.nhatromanagement.dto.MonthlyStatDTO;
import com.example.nhatromanagement.model.Bill;
import com.example.nhatromanagement.service.BillService;
import com.example.nhatromanagement.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final BillService billService;
    private final TenantService tenantService;

    @Autowired
    public HomeController(BillService billService, TenantService tenantService) {
        this.billService = billService;
        this.tenantService = tenantService;
    }

    @GetMapping("/")
    public String home(Model model) {
        // Get current year
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        // Get all bills
        List<Bill> allBills = billService.getAllBills();

        // Summary stats
        long totalRooms = tenantService.getAllTenants().size();
        long totalBills = allBills.size();
        double totalRevenue = allBills.stream().mapToDouble(Bill::getTotalAmount).sum();
        long unpaidBills = allBills.stream().filter(b -> b.getPaid() == null || !b.getPaid()).count();

        model.addAttribute("totalRooms", totalRooms);
        model.addAttribute("totalBills", totalBills);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("unpaidBills", unpaidBills);

        // Monthly stats for last 6 months (for charts)
        List<MonthlyStatDTO> monthlyStats = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusMonths(i);
            int month = date.getMonthValue();
            int year = date.getYear();

            List<Bill> monthBills = allBills.stream()
                    .filter(b -> b.getBillMonth() == month && b.getBillYear() == year)
                    .collect(Collectors.toList());

            double roomRent = monthBills.stream().mapToDouble(Bill::getRoomRent).sum();
            double electricityCost = monthBills.stream().mapToDouble(Bill::getElectricityCost).sum();
            double waterCost = monthBills.stream().mapToDouble(Bill::getWaterCost).sum();
            double totalAmount = monthBills.stream().mapToDouble(Bill::getTotalAmount).sum();

            monthlyStats.add(new MonthlyStatDTO(month, year, roomRent, electricityCost, waterCost, totalAmount,
                    monthBills.size()));
        }
        model.addAttribute("monthlyStats", monthlyStats);

        // Current month breakdown for pie chart
        var currentMonthStats = billService.getStatistics(currentMonth, currentYear);
        model.addAttribute("currentMonthStats", currentMonthStats);

        // Recent bills (last 5)
        List<Bill> recentBills = allBills.stream()
                .sorted((a, b) -> {
                    int yearCompare = Integer.compare(b.getBillYear(), a.getBillYear());
                    if (yearCompare != 0)
                        return yearCompare;
                    int monthCompare = Integer.compare(b.getBillMonth(), a.getBillMonth());
                    if (monthCompare != 0)
                        return monthCompare;
                    return Long.compare(b.getId(), a.getId());
                })
                .limit(5)
                .collect(Collectors.toList());
        model.addAttribute("recentBills", recentBills);

        return "index";
    }
}
