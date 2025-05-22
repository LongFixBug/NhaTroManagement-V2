package com.example.nhatromanagement.controller;

import com.example.nhatromanagement.model.Tenant;
import com.example.nhatromanagement.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/tenants")
public class TenantController {

    private final TenantService tenantService;

    @Autowired
    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping
    public String listTenants(Model model) {
        model.addAttribute("tenants", tenantService.getAllTenants());
        return "tenants/list"; // src/main/resources/templates/tenants/list.html
    }

    @GetMapping("/add")
    public String showAddTenantForm(Model model) {
        model.addAttribute("tenant", new Tenant());
        return "tenants/form"; // src/main/resources/templates/tenants/form.html
    }

    @PostMapping("/save")
    public String saveTenant(@ModelAttribute("tenant") Tenant tenant, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "tenants/form";
        }
        try {
            tenantService.saveTenant(tenant);
            redirectAttributes.addFlashAttribute("successMessage", "Tenant saved successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            // If saving an existing tenant (i.e., tenant.getId() != null), return to edit form
            if (tenant.getId() != null) {
                 redirectAttributes.addFlashAttribute("tenant", tenant); // send tenant back to form
                 return "redirect:/tenants/edit/" + tenant.getId();
            }
            // If creating a new tenant, return to add form
            redirectAttributes.addFlashAttribute("tenant", tenant); // send tenant back to form
            return "redirect:/tenants/add";
        }
        return "redirect:/tenants";
    }

    @GetMapping("/edit/{id}")
    public String showEditTenantForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Tenant> tenantOptional = tenantService.getTenantById(id);
        if (tenantOptional.isPresent()) {
            model.addAttribute("tenant", tenantOptional.get());
            return "tenants/form"; // Re-use the same form for editing
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Tenant not found.");
            return "redirect:/tenants";
        }
    }

    // POST for update is handled by /save as well, since tenant object will have an ID.

    @GetMapping("/delete/{id}")
    public String deleteTenant(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            tenantService.deleteTenant(id);
            redirectAttributes.addFlashAttribute("successMessage", "Tenant deleted successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            // Catching broader exceptions that might occur due to database constraints e.g. tenant has bills
            redirectAttributes.addFlashAttribute("errorMessage", "Could not delete tenant. Ensure they have no associated bills or other dependencies.");
        }
        return "redirect:/tenants";
    }
}
