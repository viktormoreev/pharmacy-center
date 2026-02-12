package com.inf.cscb869_pharmacy.customer.controller;

import com.inf.cscb869_pharmacy.customer.dto.CustomerDTO;
import com.inf.cscb869_pharmacy.customer.entity.Customer;
import com.inf.cscb869_pharmacy.customer.service.CustomerService;
import com.inf.cscb869_pharmacy.doctor.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * MVC Controller for Customer Management UI
 */
@Controller
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerViewController {

    private final CustomerService customerService;
    private final DoctorService doctorService;

    @GetMapping
    public String listCustomers(@RequestParam(required = false) String search, Model model, Authentication authentication) {
        if (isDoctorUser(authentication)) {
            String userEmail = getUserEmail(authentication);
            var doctor = doctorService.findByEmail(userEmail).orElse(null);
            model.addAttribute("isDoctorUser", true);

            if (doctor == null) {
                model.addAttribute("customers", List.of());
                model.addAttribute("errorMessage",
                        "Doctor account is not linked to a doctor record in the database. Please contact admin.");
                if (search != null && !search.trim().isEmpty()) {
                    model.addAttribute("search", search);
                }
                return "customers/customers";
            }

            List<Customer> customers = customerService.getCustomersByPrimaryDoctorId(doctor.getId());
            if (search != null && !search.trim().isEmpty()) {
                String lowered = search.trim().toLowerCase();
                customers = customers.stream()
                        .filter(c -> c.getName() != null && c.getName().toLowerCase().contains(lowered))
                        .toList();
                model.addAttribute("search", search);
            }
            model.addAttribute("currentDoctorName", doctor.getName());
            model.addAttribute("customers", customers);
            return "customers/customers";
        }

        model.addAttribute("isDoctorUser", false);
        if (search != null && !search.trim().isEmpty()) {
            model.addAttribute("customers", customerService.searchByName(search));
            model.addAttribute("search", search);
        } else {
            model.addAttribute("customers", customerService.getActiveCustomers());
        }
        return "customers/customers";
    }

    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateForm(Model model) {
        model.addAttribute("customerDTO", new CustomerDTO());
        return "customers/create-customer";
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
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

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
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

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
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

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteCustomer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            customerService.deleteCustomer(id);
            redirectAttributes.addFlashAttribute("successMessage", "Customer deactivated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete customer: " + e.getMessage());
        }
        return "redirect:/customers";
    }

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

    private boolean isDoctorUser(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_DOCTOR".equals(a.getAuthority()));
    }

    private String getUserEmail(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof OidcUser oidcUser) {
            String email = oidcUser.getEmail();
            if (email != null && !email.isBlank()) {
                return email;
            }
            Object preferredUsername = oidcUser.getClaims().get("preferred_username");
            if (preferredUsername != null) {
                return preferredUsername.toString();
            }
        }
        return authentication != null ? authentication.getName() : null;
    }
}
