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

import java.time.LocalDate;
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
        sickLeave.setIssueDate(LocalDate.now());
        
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

            var recipe = recipeService.getRecipe(recipeId);
            Long recipeDoctorId = recipe.getDoctor() != null ? recipe.getDoctor().getId() : null;
            Long recipeCustomerId = recipe.getCustomer() != null ? recipe.getCustomer().getId() : null;

            if (recipeDoctorId == null || recipeCustomerId == null) {
                redirectAttributes.addFlashAttribute("error",
                        "❌ Selected recipe is missing doctor or patient information.");
                return "redirect:/sick-leaves/create?recipeId=" + recipeId;
            }

            if (effectiveDoctorId != null && !recipeDoctorId.equals(effectiveDoctorId)) {
                redirectAttributes.addFlashAttribute("error",
                        "❌ Selected doctor does not match the selected recipe.");
                return "redirect:/sick-leaves/create?recipeId=" + recipeId;
            }

            if (customerId != null && !recipeCustomerId.equals(customerId)) {
                redirectAttributes.addFlashAttribute("error",
                        "❌ Selected patient does not match the selected recipe.");
                return "redirect:/sick-leaves/create?recipeId=" + recipeId;
            }

            if (isDoctorUser(authentication) && !recipeDoctorId.equals(effectiveDoctorId)) {
                redirectAttributes.addFlashAttribute("error",
                        "⛔ Access Denied: You can issue sick leave only for your own recipes.");
                return "redirect:/sick-leaves/create";
            }

            sickLeave.setRecipe(recipe);
            sickLeave.setIssueDate(LocalDate.now());
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

    @GetMapping("/customer/{customerId}")
    public String viewCustomerSickLeaves(@PathVariable Long customerId, Model model) {
        log.info("Viewing sick leaves for customer ID: {}", customerId);
        
        model.addAttribute("customer", customerService.getCustomerById(customerId));
        model.addAttribute("sickLeaves", sickLeaveService.getSickLeavesByCustomerId(customerId));
        
        return "sickleaves/customer-sick-leaves";
    }

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

        Long sickLeaveDoctorId = sickLeave.getRecipe() != null && sickLeave.getRecipe().getDoctor() != null
                ? sickLeave.getRecipe().getDoctor().getId()
                : null;
        if (sickLeaveDoctorId == null || !currentDoctor.getId().equals(sickLeaveDoctorId)) {
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
