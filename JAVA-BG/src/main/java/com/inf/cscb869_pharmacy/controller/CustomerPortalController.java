package com.inf.cscb869_pharmacy.controller;

import com.inf.cscb869_pharmacy.customer.entity.Customer;
import com.inf.cscb869_pharmacy.customer.service.CustomerService;
import com.inf.cscb869_pharmacy.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Self-service portal for CUSTOMER role.
 */
@Controller
@RequestMapping("/my")
@RequiredArgsConstructor
@Slf4j
public class CustomerPortalController {

    private final CustomerService customerService;
    private final ReportService reportService;

    @GetMapping("/history")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String myHistory(Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        String email = getUserEmail(authentication);
        if (email == null || email.isBlank()) {
            redirectAttributes.addFlashAttribute("error",
                    "Cannot resolve customer account. Please contact administrator.");
            return "redirect:/";
        }

        try {
            Customer customer = customerService.findByEmail(email);
            model.addAttribute("customer", customer);
            model.addAttribute("history", reportService.getPatientMedicalHistory(customer.getId()));
            return "customers/my-history";
        } catch (Exception e) {
            log.warn("Customer self-history access failed for email {}", email, e);
            redirectAttributes.addFlashAttribute("error",
                    "Customer profile is not linked to this account. Please contact administrator.");
            return "redirect:/";
        }
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
        return null;
    }
}
