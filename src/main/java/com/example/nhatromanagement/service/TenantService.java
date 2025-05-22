package com.example.nhatromanagement.service;

import com.example.nhatromanagement.model.Tenant;
import java.util.List;
import java.util.Optional;

public interface TenantService {
    Tenant saveTenant(Tenant tenant);
    Optional<Tenant> getTenantById(Long id);
    List<Tenant> getAllTenants();
    Tenant updateTenantName(Long id, String newName);
    void deleteTenant(Long id);
}
