package com.example.nhatromanagement.repository;

import com.example.nhatromanagement.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    // Basic CRUD methods are inherited
    // Add custom query methods if needed, e.g.:
    // Tenant findByName(String name);
}
