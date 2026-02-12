package com.inf.cscb869_pharmacy.customer.controller;

import com.inf.cscb869_pharmacy.customer.dto.CustomerDTO;
import com.inf.cscb869_pharmacy.customer.entity.Customer;
import com.inf.cscb869_pharmacy.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerApiController {

    private final CustomerService customerService;

    @GetMapping
    public ResponseEntity<List<CustomerDTO>> getAllCustomers() {
        List<Customer> customers = customerService.getAllCustomers();
        List<CustomerDTO> dtos = customers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/active")
    public ResponseEntity<List<CustomerDTO>> getActiveCustomers() {
        List<Customer> customers = customerService.getActiveCustomers();
        List<CustomerDTO> dtos = customers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long id) {
        Customer customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(convertToDTO(customer));
    }

    @GetMapping("/search")
    public ResponseEntity<List<CustomerDTO>> searchByName(@RequestParam String name) {
        List<Customer> customers = customerService.searchByName(name);
        List<CustomerDTO> dtos = customers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/age-range")
    public ResponseEntity<List<CustomerDTO>> findByAgeRange(
            @RequestParam Integer minAge,
            @RequestParam Integer maxAge) {
        List<Customer> customers = customerService.findByAgeRange(minAge, maxAge);
        List<CustomerDTO> dtos = customers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/allergy")
    public ResponseEntity<List<CustomerDTO>> findByAllergy(@RequestParam String allergy) {
        List<Customer> customers = customerService.findByAllergy(allergy);
        List<CustomerDTO> dtos = customers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<CustomerDTO> createCustomer(@Valid @RequestBody CustomerDTO customerDTO) {
        Customer customer = convertToEntity(customerDTO);
        Customer savedCustomer = customerService.createCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(savedCustomer));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerDTO> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerDTO customerDTO) {
        Customer customer = convertToEntity(customerDTO);
        Customer updatedCustomer = customerService.updateCustomer(id, customer);
        return ResponseEntity.ok(convertToDTO(updatedCustomer));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Void> hardDeleteCustomer(@PathVariable Long id) {
        customerService.hardDeleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/email-exists")
    public ResponseEntity<Boolean> emailExists(@RequestParam String email) {
        boolean exists = customerService.emailExists(email);
        return ResponseEntity.ok(exists);
    }

    private CustomerDTO convertToDTO(Customer customer) {
        return CustomerDTO.builder()
                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .dateOfBirth(customer.getDateOfBirth())
                .allergies(customer.getAllergies())
                .medicalHistory(customer.getMedicalHistory())
                .insuranceNumber(customer.getInsuranceNumber())
                .active(customer.getActive())
                .build();
    }

    private Customer convertToEntity(CustomerDTO dto) {
        return Customer.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .dateOfBirth(dto.getDateOfBirth())
                .allergies(dto.getAllergies())
                .medicalHistory(dto.getMedicalHistory())
                .insuranceNumber(dto.getInsuranceNumber())
                .active(dto.getActive())
                .build();
    }
}
