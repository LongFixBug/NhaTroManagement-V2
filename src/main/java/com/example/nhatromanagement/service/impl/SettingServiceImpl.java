package com.example.nhatromanagement.service.impl;

import com.example.nhatromanagement.model.Setting;
import com.example.nhatromanagement.repository.SettingRepository;
import com.example.nhatromanagement.service.SettingService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SettingServiceImpl implements SettingService {

    public static final String ELECTRICITY_PRICE_KEY = "ELECTRICITY_PRICE";
    public static final String WATER_PRICE_KEY = "WATER_PRICE";
    public static final String TRASH_FEE_KEY = "TRASH_FEE";
    public static final String WIFI_FEE_KEY = "WIFI_FEE";

    public static final String DEFAULT_ELECTRICITY_PRICE = "3000"; // VND per kWh
    public static final String DEFAULT_WATER_PRICE = "13000"; // VND per m3
    public static final String DEFAULT_TRASH_FEE = "20000"; // VND
    public static final String DEFAULT_WIFI_FEE = "50000"; // VND

    private final SettingRepository settingRepository;

    @Autowired
    public SettingServiceImpl(SettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    @PostConstruct
    @Transactional
    public void initializeDefaultSettings() {
        if (settingRepository.findBySettingKey(ELECTRICITY_PRICE_KEY).isEmpty()) {
            settingRepository.save(new Setting(ELECTRICITY_PRICE_KEY, DEFAULT_ELECTRICITY_PRICE,
                    "Price per kWh of electricity in VND"));
        }
        if (settingRepository.findBySettingKey(WATER_PRICE_KEY).isEmpty()) {
            settingRepository.save(
                    new Setting(WATER_PRICE_KEY, DEFAULT_WATER_PRICE, "Price per cubic meter (m3) of water in VND"));
        }
        if (settingRepository.findBySettingKey(TRASH_FEE_KEY).isEmpty()) {
            settingRepository.save(new Setting(TRASH_FEE_KEY, DEFAULT_TRASH_FEE, "Standard monthly trash fee in VND"));
        }
        if (settingRepository.findBySettingKey(WIFI_FEE_KEY).isEmpty()) {
            settingRepository.save(new Setting(WIFI_FEE_KEY, DEFAULT_WIFI_FEE, "Standard monthly WiFi fee in VND"));
        }
    }

    @Override
    public Optional<String> getSettingValue(String key) {
        return settingRepository.findBySettingKey(key).map(Setting::getSettingValue);
    }

    @Override
    public Optional<Double> getDoubleSettingValue(String key) {
        return getSettingValue(key).map(value -> {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                System.err.println("Error parsing double value for setting key '" + key + "': " + value);
                return null; // Or throw a custom exception
            }
        });
    }

    @Override
    @Transactional
    public void saveSetting(String key, String value, String description) {
        Setting setting = settingRepository.findBySettingKey(key)
                .orElse(new Setting(key, value, description));
        setting.setSettingValue(value);
        if (description != null && !description.isEmpty()) {
            setting.setDescription(description);
        }
        settingRepository.save(setting);
    }

    @Override
    @Transactional
    public void saveSetting(Setting setting) {
        settingRepository.save(setting);
    }

    @Override
    public List<Setting> getAllSettings() {
        return settingRepository.findAll();
    }
}
