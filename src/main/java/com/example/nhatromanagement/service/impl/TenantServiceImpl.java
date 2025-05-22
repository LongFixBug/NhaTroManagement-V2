package com.example.nhatromanagement.service.impl;

import com.example.nhatromanagement.model.Tenant;
import com.example.nhatromanagement.repository.TenantRepository;
import com.example.nhatromanagement.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;

    @Autowired
    public TenantServiceImpl(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Override
    @Transactional
    public Tenant saveTenant(Tenant tenant) {
        if (tenant.getName() == null || tenant.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tenant name cannot be empty.");
        }

        if (tenant.getId() != null) {
            // Existing tenant, handle update carefully
            Tenant existingTenant = tenantRepository.findById(tenant.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid tenant Id:" + tenant.getId()));
            
            // Update properties from the input tenant object
            existingTenant.setName(tenant.getName());
            
            // If bills were also updatable via the form, handle them here.
            // For now, since the form only sends 'name', we preserve the existing bills.
            // existingTenant.setBills(tenant.getBills()); // This would require careful handling if bills are submitted
            
            return tenantRepository.save(existingTenant);
        } else {
            // New tenant
            // Ensure bills collection is initialized if it's null and needs to be non-null
            if (tenant.getBills() == null) {
                tenant.setBills(new java.util.ArrayList<>());
            }
            return tenantRepository.save(tenant);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tenant> getTenantById(Long id) {
        return tenantRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }

    @Override
    @Transactional
    public Tenant updateTenantName(Long id, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("New tenant name cannot be empty.");
        }
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found with id: " + id));
        tenant.setName(newName);
        return tenantRepository.save(tenant);
    }

    @Override
    @Transactional
    public void deleteTenant(Long id) {
        Tenant tenant = tenantRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found with id: " + id));
        // Consider implications: what happens to bills if a tenant is deleted?
        // For now, simple deletion. Could add logic to disallow deletion if bills exist,
        // or orphan/delete bills accordingly. We might need to delete associated bills first or set tenant_id to null if allowed.
        // For now, we'll assume deletion cascades or is handled manually if foreign key constraints exist.
        tenantRepository.delete(tenant);
    }
}
