package com.inf.cscb869_pharmacy.customer.service;

import com.inf.cscb869_pharmacy.customer.entity.Customer;
import com.inf.cscb869_pharmacy.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    public Customer createCustomer(Customer customer) {
        // Validate email uniqueness
        if (customer.getEmail() != null && customerRepository.existsByEmail(customer.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + customer.getEmail());
        }
        // Set active to true by default
        if (customer.getActive() == null) {
            customer.setActive(true);
        }
        return customerRepository.save(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> getActiveCustomers() {
        return customerRepository.findByActiveTrue();
    }

    @Override
    public Customer updateCustomer(Long id, Customer customer) {
        Customer existingCustomer = getCustomerById(id);
        
        // Check email uniqueness if changing email
        if (customer.getEmail() != null && 
            !customer.getEmail().equals(existingCustomer.getEmail()) &&
            customerRepository.existsByEmail(customer.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + customer.getEmail());
        }

        // Update fields
        existingCustomer.setName(customer.getName());
        existingCustomer.setEmail(customer.getEmail());
        existingCustomer.setPhone(customer.getPhone());
        existingCustomer.setAddress(customer.getAddress());
        existingCustomer.setDateOfBirth(customer.getDateOfBirth());
        existingCustomer.setAllergies(customer.getAllergies());
        existingCustomer.setMedicalHistory(customer.getMedicalHistory());
        existingCustomer.setInsuranceNumber(customer.getInsuranceNumber());
        
        if (customer.getActive() != null) {
            existingCustomer.setActive(customer.getActive());
        }

        return customerRepository.save(existingCustomer);
    }

    @Override
    public void deleteCustomer(Long id) {
        Customer customer = getCustomerById(id);
        customer.setActive(false);
        customerRepository.save(customer);
    }

    @Override
    public void hardDeleteCustomer(Long id) {
        Customer customer = getCustomerById(id);
        customerRepository.delete(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public Customer findByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> searchByName(String name) {
        return customerRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> findByAgeRange(Integer minAge, Integer maxAge) {
        return customerRepository.findAll().stream()
                .filter(c -> c.getAge() != null)
                .filter(c -> c.getAge() >= minAge && c.getAge() <= maxAge)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> findByAllergy(String allergy) {
        return customerRepository.findByAllergiesContaining(allergy);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return customerRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> getCustomersByPrimaryDoctorId(Long primaryDoctorId) {
        return customerRepository.findByPrimaryDoctorId(primaryDoctorId).stream()
                .filter(c -> Boolean.TRUE.equals(c.getActive()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countCustomers() {
        return customerRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveCustomers() {
        return customerRepository.findByActiveTrue().size();
    }
}
