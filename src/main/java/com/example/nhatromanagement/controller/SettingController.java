package com.example.nhatromanagement.controller;

import com.example.nhatromanagement.model.Setting;
import com.example.nhatromanagement.service.SettingService;
import com.example.nhatromanagement.service.impl.SettingServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.context.i18n.LocaleContextHolder;
import java.util.Locale;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/settings")
public class SettingController {

    private final SettingService settingService;
    private final MessageSource messageSource;

    @Autowired
    public SettingController(SettingService settingService, MessageSource messageSource) {
        this.settingService = settingService;
        this.messageSource = messageSource;
    }

    @GetMapping
    public String viewSettingsPage(Model model) {
        List<Setting> settings = settingService.getAllSettings();
        model.addAttribute("settings", settings);
        Locale locale = LocaleContextHolder.getLocale();
        model.addAttribute("pageTitle", messageSource.getMessage("settings.title", null, locale));

        // Add specific settings to the model
        settings.forEach(setting -> {
            if (SettingServiceImpl.ELECTRICITY_PRICE_KEY.equals(setting.getSettingKey())) {
                model.addAttribute("electricityPrice", setting.getSettingValue());
            }
            if (SettingServiceImpl.WATER_PRICE_KEY.equals(setting.getSettingKey())) {
                model.addAttribute("waterPrice", setting.getSettingValue());
            }
            if (SettingServiceImpl.TRASH_FEE_KEY.equals(setting.getSettingKey())) {
                model.addAttribute("trashFee", setting.getSettingValue());
            }
            if (SettingServiceImpl.WIFI_FEE_KEY.equals(setting.getSettingKey())) {
                model.addAttribute("wifiFee", setting.getSettingValue());
            }
        });
        return "settings/form";
    }

    @PostMapping("/save")
    public String saveSettings(@RequestParam Map<String, String> allParams, RedirectAttributes redirectAttributes) {
        try {
            String electricityPrice = allParams.get(SettingServiceImpl.ELECTRICITY_PRICE_KEY);
            String waterPrice = allParams.get(SettingServiceImpl.WATER_PRICE_KEY);
            String trashFee = allParams.get(SettingServiceImpl.TRASH_FEE_KEY);
            String wifiFee = allParams.get(SettingServiceImpl.WIFI_FEE_KEY);

            if (electricityPrice != null) {
                settingService.saveSetting(SettingServiceImpl.ELECTRICITY_PRICE_KEY, electricityPrice,
                        "Price per kWh of electricity in VND");
            }
            if (waterPrice != null) {
                settingService.saveSetting(SettingServiceImpl.WATER_PRICE_KEY, waterPrice,
                        "Price per cubic meter (m3) of water in VND");
            }
            if (trashFee != null) {
                settingService.saveSetting(SettingServiceImpl.TRASH_FEE_KEY, trashFee,
                        "Standard monthly trash fee in VND");
            }
            if (wifiFee != null) {
                settingService.saveSetting(SettingServiceImpl.WIFI_FEE_KEY, wifiFee,
                        "Standard monthly WiFi fee in VND");
            }

            redirectAttributes.addFlashAttribute("successMessage",
                    messageSource.getMessage("settings.success.updated", null, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            System.err.println("Error saving settings: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("settings.error.updated", null, LocaleContextHolder.getLocale()));
        }
        return "redirect:/settings";
    }
}
