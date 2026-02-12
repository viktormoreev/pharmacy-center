package com.inf.cscb869_pharmacy.customer.service;

import com.inf.cscb869_pharmacy.customer.entity.Customer;

import java.util.List;

public interface CustomerService {

    /**
     * Create a new customer
     */
    Customer createCustomer(Customer customer);

    /**
     * Get customer by ID
     */
    Customer getCustomerById(Long id);

    /**
     * Get all customers
     */
    List<Customer> getAllCustomers();

    /**
     * Get all active customers
     */
    List<Customer> getActiveCustomers();
    
    /**
     * Count total customers
     */
    long countCustomers();
    
    /**
     * Count active customers
     */
    long countActiveCustomers();

    /**
     * Update customer
     */
    Customer updateCustomer(Long id, Customer customer);

    /**
     * Delete customer (soft delete - set active to false)
     */
    void deleteCustomer(Long id);

    /**
     * Hard delete customer
     */
    void hardDeleteCustomer(Long id);

    /**
     * Find customer by email
     */
    Customer findByEmail(String email);

    /**
     * Search customers by name
     */
    List<Customer> searchByName(String name);

    /**
     * Find customers by age range
     */
    List<Customer> findByAgeRange(Integer minAge, Integer maxAge);

    /**
     * Find customers with specific allergy
     */
    List<Customer> findByAllergy(String allergy);

    /**
     * Check if email already exists
     */
    boolean emailExists(String email);

    /**
     * Get active customers assigned to a primary doctor.
     */
    List<Customer> getCustomersByPrimaryDoctorId(Long primaryDoctorId);
}
