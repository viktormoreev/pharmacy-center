package com.inf.cscb869_pharmacy.customer.controller;

import com.inf.cscb869_pharmacy.customer.dto.CustomerDTO;
import com.inf.cscb869_pharmacy.customer.entity.Customer;
import com.inf.cscb869_pharmacy.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MVC Controller for Customer Management UI
 */
@Controller
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerViewController {

    private final CustomerService customerService;

    /**
     * List all active customers
     */
    @GetMapping
    public String listCustomers(@RequestParam(required = false) String search, Model model) {
        if (search != null && !search.trim().isEmpty()) {
            model.addAttribute("customers", customerService.searchByName(search));
            model.addAttribute("search", search);
        } else {
            model.addAttribute("customers", customerService.getActiveCustomers());
        }
        return "customers/customers";
    }

    /**
     * Show create customer form
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("customerDTO", new CustomerDTO());
        return "customers/create-customer";
    }

    /**
     * Handle create customer form submission
     */
    @PostMapping("/create")
    public String createCustomer(@Valid @ModelAttribute("customerDTO") CustomerDTO customerDTO,
                                  BindingResult result,
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "customers/create-customer";
        }

        try {
            Customer customer = convertToEntity(customerDTO);
            customerService.createCustomer(customer);
            redirectAttributes.addFlashAttribute("successMessage", "Customer created successfully!");
            return "redirect:/customers";
        } catch (IllegalArgumentException e) {
            result.rejectValue("email", "error.customerDTO", e.getMessage());
            return "customers/create-customer";
        }
    }

    /**
     * Show customer details
     */
    @GetMapping("/{id}")
    public String viewCustomer(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Customer customer = customerService.getCustomerById(id);
            model.addAttribute("customer", customer);
            return "customers/view-customer";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Customer not found!");
            return "redirect:/customers";
        }
    }

    /**
     * Show edit customer form
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Customer customer = customerService.getCustomerById(id);
            CustomerDTO customerDTO = convertToDTO(customer);
            model.addAttribute("customerDTO", customerDTO);
            model.addAttribute("customerId", id);
            return "customers/edit-customer";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Customer not found!");
            return "redirect:/customers";
        }
    }

    /**
     * Handle edit customer form submission
     */
    @PostMapping("/edit/{id}")
    public String updateCustomer(@PathVariable Long id,
                                  @Valid @ModelAttribute("customerDTO") CustomerDTO customerDTO,
                                  BindingResult result,
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "customers/edit-customer";
        }

        try {
            Customer customer = convertToEntity(customerDTO);
            customerService.updateCustomer(id, customer);
            redirectAttributes.addFlashAttribute("successMessage", "Customer updated successfully!");
            return "redirect:/customers";
        } catch (IllegalArgumentException e) {
            result.rejectValue("email", "error.customerDTO", e.getMessage());
            return "customers/edit-customer";
        }
    }

    /**
     * Soft delete customer
     */
    @PostMapping("/delete/{id}")
    public String deleteCustomer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            customerService.deleteCustomer(id);
            redirectAttributes.addFlashAttribute("successMessage", "Customer deactivated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete customer: " + e.getMessage());
        }
        return "redirect:/customers";
    }

    // Helper methods to convert between Customer and CustomerDTO
    private Customer convertToEntity(CustomerDTO dto) {
        return Customer.builder()
                .name(dto.getName())
                .age(dto.getAge())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .dateOfBirth(dto.getDateOfBirth())
                .allergies(dto.getAllergies())
                .medicalHistory(dto.getMedicalHistory())
                .insuranceNumber(dto.getInsuranceNumber())
                .active(true)
                .build();
    }

    private CustomerDTO convertToDTO(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setName(customer.getName());
        dto.setAge(customer.getAge());
        dto.setEmail(customer.getEmail());
        dto.setPhone(customer.getPhone());
        dto.setAddress(customer.getAddress());
        dto.setDateOfBirth(customer.getDateOfBirth());
        dto.setAllergies(customer.getAllergies());
        dto.setMedicalHistory(customer.getMedicalHistory());
        dto.setInsuranceNumber(customer.getInsuranceNumber());
        return dto;
    }
}
