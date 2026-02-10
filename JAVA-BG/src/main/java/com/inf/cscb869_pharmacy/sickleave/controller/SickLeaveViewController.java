package com.inf.cscb869_pharmacy.sickleave.controller;

import com.inf.cscb869_pharmacy.customer.service.CustomerService;
import com.inf.cscb869_pharmacy.doctor.service.DoctorService;
import com.inf.cscb869_pharmacy.recipe.service.RecipeService;
import com.inf.cscb869_pharmacy.sickleave.entity.SickLeave;
import com.inf.cscb869_pharmacy.sickleave.entity.SickLeaveStatus;
import com.inf.cscb869_pharmacy.sickleave.service.SickLeaveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

/**
 * View Controller for Sick Leave management
 * Контролер за управление на болнични листове (UI)
 */
@Controller
@RequestMapping("/sick-leaves")
@RequiredArgsConstructor
@Slf4j
public class SickLeaveViewController {

    private final SickLeaveService sickLeaveService;
    private final RecipeService recipeService;
    private final CustomerService customerService;
    private final DoctorService doctorService;

    /**
     * List all sick leaves
     * GET /sick-leaves
     */
    @GetMapping
    public String listSickLeaves(Model model, Authentication authentication) {
        log.info("Displaying all sick leaves");

        if (isDoctorUser(authentication)) {
            var currentDoctor = findDoctorByEmail(getUserEmail(authentication));
            if (currentDoctor == null) {
                model.addAttribute("sickLeaves", List.of());
                model.addAttribute("error", "Doctor account is not linked to a doctor record in the database. Please contact admin.");
                model.addAttribute("isDoctorUser", true);
                return "sickleaves/sick-leaves";
            }
            model.addAttribute("sickLeaves", sickLeaveService.getSickLeavesByDoctorId(currentDoctor.getId()));
            model.addAttribute("isDoctorUser", true);
            return "sickleaves/sick-leaves";
        }

        model.addAttribute("sickLeaves", sickLeaveService.getAllSickLeaves());
        model.addAttribute("isDoctorUser", false);
        return "sickleaves/sick-leaves";
    }

    /**
     * Show form to create new sick leave
     * GET /sick-leaves/create
     */
    @GetMapping("/create")
    public String showCreateForm(
            @RequestParam(required = false) Long recipeId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long doctorId,
            Model model,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        log.info("Showing create sick leave form");
        
        SickLeave sickLeave = new SickLeave();
        sickLeave.setLeaveNumber(sickLeaveService.generateLeaveNumber());
        
        model.addAttribute("sickLeave", sickLeave);
        model.addAttribute("statuses", SickLeaveStatus.values());
        model.addAttribute("customers", customerService.getAllCustomers());

        if (recipeId != null) {
            var recipe = recipeService.getRecipe(recipeId);
            model.addAttribute("recipe", recipe);
            if (customerId == null && recipe.getCustomer() != null) {
                model.addAttribute("selectedCustomer", recipe.getCustomer());
            }
        }
        
        if (customerId != null) {
            model.addAttribute("selectedCustomer", customerService.getCustomerById(customerId));
        }

        Long selectedCustomerId = null;
        if (model.containsAttribute("selectedCustomer")) {
            var selectedCustomer = (com.inf.cscb869_pharmacy.customer.entity.Customer) model.asMap().get("selectedCustomer");
            if (selectedCustomer != null) {
                selectedCustomerId = selectedCustomer.getId();
            }
        }
        final Long selectedCustomerIdFinal = selectedCustomerId;
        
        if (isDoctorUser(authentication)) {
            var currentDoctor = findDoctorByEmail(getUserEmail(authentication));
            if (currentDoctor == null) {
                redirectAttributes.addFlashAttribute("error",
                        "Doctor account is not linked to a doctor record in the database. Please contact admin.");
                return "redirect:/sick-leaves";
            }
            model.addAttribute("selectedDoctor", currentDoctor);
            model.addAttribute("doctors", List.of(currentDoctor));
            model.addAttribute("isDoctorUser", true);
            var recipes = recipeService.getRecipes().stream()
                    .filter(r -> r.getDoctor() != null && r.getDoctor().getId().equals(currentDoctor.getId()));
            if (selectedCustomerIdFinal != null) {
                recipes = recipes.filter(r -> r.getCustomer() != null && r.getCustomer().getId().equals(selectedCustomerIdFinal));
            }
            model.addAttribute("recipes", recipes.collect(Collectors.toList()));
        } else {
            if (doctorId != null) {
                model.addAttribute("selectedDoctor", doctorService.getDoctor(doctorId));
            }
            model.addAttribute("doctors", doctorService.getDoctors());
            model.addAttribute("isDoctorUser", false);
            var recipes = recipeService.getRecipes().stream();
            if (selectedCustomerIdFinal != null) {
                recipes = recipes.filter(r -> r.getCustomer() != null && r.getCustomer().getId().equals(selectedCustomerIdFinal));
            }
            model.addAttribute("recipes", recipes.collect(Collectors.toList()));
        }
        
        return "sickleaves/create-sick-leave";
    }

    /**
     * Create new sick leave
     * POST /sick-leaves/create
     */
    @PostMapping("/create")
    public String createSickLeave(
            @ModelAttribute SickLeave sickLeave,
            @RequestParam Long recipeId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long doctorId,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {
        log.info("Creating new sick leave");
        
        try {
            Long effectiveDoctorId = doctorId;
            if (isDoctorUser(authentication)) {
                var currentDoctor = findDoctorByEmail(getUserEmail(authentication));
                if (currentDoctor == null) {
                    redirectAttributes.addFlashAttribute("error",
                            "Doctor account is not linked to a doctor record in the database. Please contact admin.");
                    return "redirect:/sick-leaves";
                }
                effectiveDoctorId = currentDoctor.getId();
            }

            if (effectiveDoctorId == null) {
                redirectAttributes.addFlashAttribute("error", "❌ Doctor is required");
                return "redirect:/sick-leaves/create";
            }

            var recipe = recipeService.getRecipe(recipeId);
            sickLeave.setRecipe(recipe);
            sickLeave.setDoctor(doctorService.getDoctor(effectiveDoctorId));

            Long effectiveCustomerId = customerId != null ? customerId : recipe.getCustomer().getId();
            if (recipe.getCustomer() != null && !recipe.getCustomer().getId().equals(effectiveCustomerId)) {
                redirectAttributes.addFlashAttribute("error",
                        "❌ Selected patient does not match the selected recipe.");
                return "redirect:/sick-leaves/create?recipeId=" + recipeId;
            }
            sickLeave.setCustomer(customerService.getCustomerById(effectiveCustomerId));

            if (isDoctorUser(authentication) && recipe.getDoctor() != null &&
                    !recipe.getDoctor().getId().equals(effectiveDoctorId)) {
                redirectAttributes.addFlashAttribute("error",
                        "⛔ Access Denied: You can issue sick leave only for your own recipes.");
                return "redirect:/sick-leaves/create";
            }
            
            sickLeaveService.createSickLeave(sickLeave);
            redirectAttributes.addFlashAttribute("success", 
                "✅ Sick leave created successfully! Number: " + sickLeave.getLeaveNumber());
            
            return "redirect:/sick-leaves";
            
        } catch (Exception e) {
            log.error("Error creating sick leave", e);
            redirectAttributes.addFlashAttribute("error", "❌ Error creating sick leave: " + e.getMessage());
            return "redirect:/sick-leaves/create";
        }
    }

    /**
     * Show sick leave details
     * GET /sick-leaves/view/{id}
     */
    @GetMapping("/view/{id}")
    public String viewSickLeave(@PathVariable Long id, Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        log.info("Viewing sick leave with ID: {}", id);
        
        SickLeave sickLeave = sickLeaveService.getSickLeaveById(id);
        if (!hasDoctorAccess(authentication, sickLeave, redirectAttributes)) {
            return "redirect:/sick-leaves";
        }

        model.addAttribute("sickLeave", sickLeave);
        
        // Calculate additional information
        model.addAttribute("isActive", sickLeave.isCurrentlyActive());
        model.addAttribute("isExpired", sickLeave.isExpired());
        
        return "sickleaves/view-sick-leave";
    }

    /**
     * Show form to edit sick leave
     * GET /sick-leaves/edit/{id}
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        log.info("Showing edit form for sick leave ID: {}", id);

        SickLeave sickLeave = sickLeaveService.getSickLeaveById(id);
        if (!hasDoctorAccess(authentication, sickLeave, redirectAttributes)) {
            return "redirect:/sick-leaves";
        }

        model.addAttribute("sickLeave", sickLeave);
        model.addAttribute("statuses", SickLeaveStatus.values());
        
        return "sickleaves/edit-sick-leave";
    }

    /**
     * Update sick leave
     * POST /sick-leaves/edit/{id}
     */
    @PostMapping("/edit/{id}")
    public String updateSickLeave(
            @PathVariable Long id,
            @ModelAttribute SickLeave sickLeave,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {
        log.info("Updating sick leave with ID: {}", id);
        
        try {
            SickLeave existing = sickLeaveService.getSickLeaveById(id);
            if (!hasDoctorAccess(authentication, existing, redirectAttributes)) {
                return "redirect:/sick-leaves";
            }

            sickLeaveService.updateSickLeave(id, sickLeave);
            redirectAttributes.addFlashAttribute("success", "✅ Sick leave updated successfully!");
            return "redirect:/sick-leaves/view/" + id;
            
        } catch (Exception e) {
            log.error("Error updating sick leave", e);
            redirectAttributes.addFlashAttribute("error", "❌ Error updating sick leave: " + e.getMessage());
            return "redirect:/sick-leaves/edit/" + id;
        }
    }

    /**
     * Delete sick leave
     * POST /sick-leaves/delete/{id}
     */
    @PostMapping("/delete/{id}")
    public String deleteSickLeave(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {
        log.info("Deleting sick leave with ID: {}", id);
        
        try {
            SickLeave existing = sickLeaveService.getSickLeaveById(id);
            if (!hasDoctorAccess(authentication, existing, redirectAttributes)) {
                return "redirect:/sick-leaves";
            }

            sickLeaveService.deleteSickLeave(id);
            redirectAttributes.addFlashAttribute("success", "✅ Sick leave deleted successfully!");
            
        } catch (Exception e) {
            log.error("Error deleting sick leave", e);
            redirectAttributes.addFlashAttribute("error", "❌ Error deleting sick leave: " + e.getMessage());
        }
        
        return "redirect:/sick-leaves";
    }

    /**
     * Show form to extend sick leave
     * GET /sick-leaves/extend/{id}
     */
    @GetMapping("/extend/{id}")
    public String showExtendForm(@PathVariable Long id, Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        log.info("Showing extend form for sick leave ID: {}", id);
        SickLeave sickLeave = sickLeaveService.getSickLeaveById(id);
        if (!hasDoctorAccess(authentication, sickLeave, redirectAttributes)) {
            return "redirect:/sick-leaves";
        }

        model.addAttribute("sickLeave", sickLeave);
        return "sickleaves/extend-sick-leave";
    }

    /**
     * Extend sick leave
     * POST /sick-leaves/extend/{id}
     */
    @PostMapping("/extend/{id}")
    public String extendSickLeave(
            @PathVariable Long id,
            @RequestParam Integer additionalDays,
            @RequestParam String reason,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {
        log.info("Extending sick leave {} by {} days", id, additionalDays);
        
        try {
            SickLeave existing = sickLeaveService.getSickLeaveById(id);
            if (!hasDoctorAccess(authentication, existing, redirectAttributes)) {
                return "redirect:/sick-leaves";
            }

            sickLeaveService.extendSickLeave(id, additionalDays, reason);
            redirectAttributes.addFlashAttribute("success", 
                "✅ Sick leave extended by " + additionalDays + " days!");
            return "redirect:/sick-leaves/view/" + id;
            
        } catch (Exception e) {
            log.error("Error extending sick leave", e);
            redirectAttributes.addFlashAttribute("error", "❌ Error extending sick leave: " + e.getMessage());
            return "redirect:/sick-leaves/extend/" + id;
        }
    }

    /**
     * Cancel sick leave
     * POST /sick-leaves/cancel/{id}
     */
    @PostMapping("/cancel/{id}")
    public String cancelSickLeave(
            @PathVariable Long id,
            @RequestParam String reason,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {
        log.info("Cancelling sick leave {}", id);
        
        try {
            SickLeave existing = sickLeaveService.getSickLeaveById(id);
            if (!hasDoctorAccess(authentication, existing, redirectAttributes)) {
                return "redirect:/sick-leaves";
            }

            sickLeaveService.cancelSickLeave(id, reason);
            redirectAttributes.addFlashAttribute("success", "✅ Sick leave cancelled!");
            return "redirect:/sick-leaves/view/" + id;
            
        } catch (Exception e) {
            log.error("Error cancelling sick leave", e);
            redirectAttributes.addFlashAttribute("error", "❌ Error cancelling sick leave: " + e.getMessage());
            return "redirect:/sick-leaves/view/" + id;
        }
    }

    /**
     * Complete sick leave
     * POST /sick-leaves/complete/{id}
     */
    @PostMapping("/complete/{id}")
    public String completeSickLeave(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {
        log.info("Marking sick leave {} as completed", id);
        
        try {
            SickLeave existing = sickLeaveService.getSickLeaveById(id);
            if (!hasDoctorAccess(authentication, existing, redirectAttributes)) {
                return "redirect:/sick-leaves";
            }

            sickLeaveService.completeSickLeave(id);
            redirectAttributes.addFlashAttribute("success", "✅ Sick leave marked as completed!");
            return "redirect:/sick-leaves/view/" + id;
            
        } catch (Exception e) {
            log.error("Error completing sick leave", e);
            redirectAttributes.addFlashAttribute("error", "❌ Error completing sick leave: " + e.getMessage());
            return "redirect:/sick-leaves/view/" + id;
        }
    }

    /**
     * View sick leaves by customer
     * GET /sick-leaves/customer/{customerId}
     */
    @GetMapping("/customer/{customerId}")
    public String viewCustomerSickLeaves(@PathVariable Long customerId, Model model) {
        log.info("Viewing sick leaves for customer ID: {}", customerId);
        
        model.addAttribute("customer", customerService.getCustomerById(customerId));
        model.addAttribute("sickLeaves", sickLeaveService.getSickLeavesByCustomerId(customerId));
        
        return "sickleaves/customer-sick-leaves";
    }

    /**
     * View sick leaves by doctor
     * GET /sick-leaves/doctor/{doctorId}
     */
    @GetMapping("/doctor/{doctorId}")
    public String viewDoctorSickLeaves(@PathVariable Long doctorId, Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        log.info("Viewing sick leaves for doctor ID: {}", doctorId);

        if (isDoctorUser(authentication)) {
            var currentDoctor = findDoctorByEmail(getUserEmail(authentication));
            if (currentDoctor == null) {
                redirectAttributes.addFlashAttribute("error",
                        "Doctor account is not linked to a doctor record in the database. Please contact admin.");
                return "redirect:/sick-leaves";
            }
            if (!currentDoctor.getId().equals(doctorId)) {
                redirectAttributes.addFlashAttribute("error",
                        "⛔ Access Denied: You can only view your own sick leaves.");
                return "redirect:/sick-leaves";
            }
        }
        
        model.addAttribute("doctor", doctorService.getDoctor(doctorId));
        model.addAttribute("sickLeaves", sickLeaveService.getSickLeavesByDoctorId(doctorId));
        
        return "sickleaves/doctor-sick-leaves";
    }

    private boolean hasDoctorAccess(Authentication authentication, SickLeave sickLeave, RedirectAttributes redirectAttributes) {
        if (!isDoctorUser(authentication)) {
            return true;
        }

        var currentDoctor = findDoctorByEmail(getUserEmail(authentication));
        if (currentDoctor == null) {
            redirectAttributes.addFlashAttribute("error",
                    "Doctor account is not linked to a doctor record in the database. Please contact admin.");
            return false;
        }

        if (!currentDoctor.getId().equals(sickLeave.getDoctor().getId())) {
            redirectAttributes.addFlashAttribute("error",
                    "⛔ Access Denied: You can only manage your own sick leaves.");
            return false;
        }
        return true;
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

    private com.inf.cscb869_pharmacy.doctor.entity.Doctor findDoctorByEmail(String email) {
        return doctorService.findByEmail(email).orElse(null);
    }

    private boolean isDoctorUser(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_DOCTOR".equals(a.getAuthority()));
    }
}
