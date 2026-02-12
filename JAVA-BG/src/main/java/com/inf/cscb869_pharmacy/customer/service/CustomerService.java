package com.inf.cscb869_pharmacy.customer.service;

import com.inf.cscb869_pharmacy.customer.entity.Customer;

import java.util.List;

public interface CustomerService {

    Customer createCustomer(Customer customer);

    Customer getCustomerById(Long id);

    List<Customer> getAllCustomers();

    List<Customer> getActiveCustomers();

    long countCustomers();

    long countActiveCustomers();

    Customer updateCustomer(Long id, Customer customer);

    void deleteCustomer(Long id);

    void hardDeleteCustomer(Long id);

    Customer findByEmail(String email);


    List<Customer> searchByName(String name);

    List<Customer> findByAgeRange(Integer minAge, Integer maxAge);

    List<Customer> findByAllergy(String allergy);

    boolean emailExists(String email);

    List<Customer> getCustomersByPrimaryDoctorId(Long primaryDoctorId);
}
