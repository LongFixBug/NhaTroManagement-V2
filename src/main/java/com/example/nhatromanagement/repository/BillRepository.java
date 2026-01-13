package com.example.nhatromanagement.repository;

import com.example.nhatromanagement.model.Bill;
import com.example.nhatromanagement.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    /**
     * Finds the latest bill for a given tenant based on year and month.
     * This is useful for fetching the previous month's electricity and water meter
     * readings.
     * 
     * @param tenant The tenant for whom to find the latest bill.
     * @return An Optional containing the latest Bill if found, or an empty Optional
     *         otherwise.
     */
    Optional<Bill> findTopByTenantOrderByBillYearDescBillMonthDescIdDesc(Tenant tenant);

    /**
     * Finds a bill for a specific tenant, month, and year.
     * 
     * @param tenant    The tenant.
     * @param billMonth The month of the bill.
     * @param billYear  The year of the bill.
     * @return An Optional containing the Bill if found, or an empty Optional
     *         otherwise.
     */
    Optional<Bill> findByTenantAndBillMonthAndBillYear(Tenant tenant, int billMonth, int billYear);

    /**
     * Finds all bills for a specific month and year.
     * 
     * @param billMonth The month of the bill.
     * @param billYear  The year of the bill.
     * @return A list of Bills for the given month and year.
     */
    java.util.List<Bill> findByBillMonthAndBillYear(int billMonth, int billYear);

    /**
     * Finds all bills for a specific year.
     * 
     * @param billYear The year of the bill.
     * @return A list of Bills for the given year.
     */
    java.util.List<Bill> findByBillYear(int billYear);

    /**
     * Finds all bills for a specific tenant.
     */
    java.util.List<Bill> findByTenant(Tenant tenant);

    /**
     * Finds all bills for a specific tenant in a specific month and year.
     */
    java.util.List<Bill> findByTenantAndBillMonthAndBillYearOrderByBillMonthDesc(Tenant tenant, int billMonth,
            int billYear);

    /**
     * Finds all bills for a specific tenant in a specific year.
     */
    java.util.List<Bill> findByTenantAndBillYear(Tenant tenant, int billYear);
}
