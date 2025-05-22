package com.example.nhatromanagement.service;

import com.example.nhatromanagement.model.Setting;

import java.util.List;
import java.util.Optional;

public interface SettingService {
    Optional<String> getSettingValue(String key);
    Optional<Double> getDoubleSettingValue(String key);
    void saveSetting(String key, String value, String description);
    void saveSetting(Setting setting);
    List<Setting> getAllSettings();
    void initializeDefaultSettings();
}
