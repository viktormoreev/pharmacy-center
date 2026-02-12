package com.inf.cscb869_pharmacy.diagnosis.controller;

import com.inf.cscb869_pharmacy.diagnosis.entity.Diagnosis;
import com.inf.cscb869_pharmacy.diagnosis.entity.DiagnosisSeverity;
import com.inf.cscb869_pharmacy.diagnosis.service.DiagnosisService;
import com.inf.cscb869_pharmacy.doctor.service.DoctorService;
import com.inf.cscb869_pharmacy.recipe.service.RecipeService;
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
 * View Controller for Diagnosis management
 * Контролер за управление на диагнози (UI)
 */
@Controller
@RequestMapping("/diagnoses")
@RequiredArgsConstructor
@Slf4j
public class DiagnosisViewController {

    private final DiagnosisService diagnosisService;
    private final RecipeService recipeService;
    private final DoctorService doctorService;

    @GetMapping
    public String listDiagnoses(Model model, Authentication authentication) {
        log.info("Displaying all diagnoses");

        if (isDoctorUser(authentication)) {
            var currentDoctor = findDoctorByEmail(getUserEmail(authentication));
            if (currentDoctor == null) {
                model.addAttribute("diagnoses", List.of());
                model.addAttribute("error", "Doctor account is not linked to a doctor record in the database. Please contact admin.");
                return "diagnoses/diagnoses";
            }
            model.addAttribute("diagnoses", diagnosisService.getAllDiagnoses().stream()
                    .filter(d -> d.getRecipe() != null && d.getRecipe().getDoctor() != null
                            && d.getRecipe().getDoctor().getId().equals(currentDoctor.getId()))
                    .collect(Collectors.toList()));
            return "diagnoses/diagnoses";
        }

        model.addAttribute("diagnoses", diagnosisService.getAllDiagnoses());
        return "diagnoses/diagnoses";
    }

    @GetMapping("/create")
    public String showCreateForm(
            @RequestParam(required = false) Long recipeId,
            Model model,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        log.info("Showing create diagnosis form");
        
        model.addAttribute("diagnosis", new Diagnosis());
        model.addAttribute("severities", DiagnosisSeverity.values());
        
        if (recipeId != null) {
            model.addAttribute("recipe", recipeService.getRecipe(recipeId));
        }

        if (isDoctorUser(authentication)) {
            var currentDoctor = findDoctorByEmail(getUserEmail(authentication));
            if (currentDoctor == null) {
                redirectAttributes.addFlashAttribute("error",
                        "Doctor account is not linked to a doctor record in the database. Please contact admin.");
                return "redirect:/diagnoses";
            }

            model.addAttribute("recipes", recipeService.getRecipes().stream()
                    .filter(r -> r.getDoctor() != null && r.getDoctor().getId().equals(currentDoctor.getId()))
                    .collect(Collectors.toList()));
        } else {
            model.addAttribute("recipes", recipeService.getRecipes());
        }
        
        return "diagnoses/create-diagnosis";
    }

    @PostMapping("/create")
    public String createDiagnosis(
            @ModelAttribute Diagnosis diagnosis,
            @RequestParam Long recipeId,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {
        log.info("Creating new diagnosis: {}", diagnosis.getName());
        
        try {
            var recipe = recipeService.getRecipe(recipeId);
            diagnosis.setRecipe(recipe);

            if (isDoctorUser(authentication)) {
                var currentDoctor = findDoctorByEmail(getUserEmail(authentication));
                if (currentDoctor == null) {
                    redirectAttributes.addFlashAttribute("error",
                            "Doctor account is not linked to a doctor record in the database. Please contact admin.");
                    return "redirect:/diagnoses";
                }
                if (recipe.getDoctor() == null || !recipe.getDoctor().getId().equals(currentDoctor.getId())) {
                    redirectAttributes.addFlashAttribute("error",
                            "⛔ Access Denied: You can add diagnosis only to your own recipes.");
                    return "redirect:/diagnoses/create";
                }
            }
            
            diagnosisService.createDiagnosis(diagnosis);
            redirectAttributes.addFlashAttribute("success", "✅ Diagnosis created successfully!");
            
            return "redirect:/diagnoses";
            
        } catch (Exception e) {
            log.error("Error creating diagnosis", e);
            redirectAttributes.addFlashAttribute("error", "❌ Error creating diagnosis: " + e.getMessage());
            return "redirect:/diagnoses/create";
        }
    }

    /**
     * Show diagnosis details
     * GET /diagnoses/view/{id}
     */
    @GetMapping("/view/{id}")
    public String viewDiagnosis(@PathVariable Long id, Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        log.info("Viewing diagnosis with ID: {}", id);
        Diagnosis diagnosis = diagnosisService.getDiagnosisById(id);
        if (!hasDoctorAccess(authentication, diagnosis, redirectAttributes)) {
            return "redirect:/diagnoses";
        }
        model.addAttribute("diagnosis", diagnosis);
        return "diagnoses/view-diagnosis";
    }

    /**
     * Show form to edit diagnosis
     * GET /diagnoses/edit/{id}
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        log.info("Showing edit form for diagnosis ID: {}", id);

        Diagnosis diagnosis = diagnosisService.getDiagnosisById(id);
        if (!hasDoctorAccess(authentication, diagnosis, redirectAttributes)) {
            return "redirect:/diagnoses";
        }

        model.addAttribute("diagnosis", diagnosis);
        model.addAttribute("severities", DiagnosisSeverity.values());
        
        return "diagnoses/edit-diagnosis";
    }

    /**
     * Update diagnosis
     * POST /diagnoses/edit/{id}
     */
    @PostMapping("/edit/{id}")
    public String updateDiagnosis(
            @PathVariable Long id,
            @ModelAttribute Diagnosis diagnosis,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {
        log.info("Updating diagnosis with ID: {}", id);
        
        try {
            Diagnosis existing = diagnosisService.getDiagnosisById(id);
            if (!hasDoctorAccess(authentication, existing, redirectAttributes)) {
                return "redirect:/diagnoses";
            }

            diagnosisService.updateDiagnosis(id, diagnosis);
            redirectAttributes.addFlashAttribute("success", "✅ Diagnosis updated successfully!");
            return "redirect:/diagnoses/view/" + id;
            
        } catch (Exception e) {
            log.error("Error updating diagnosis", e);
            redirectAttributes.addFlashAttribute("error", "❌ Error updating diagnosis: " + e.getMessage());
            return "redirect:/diagnoses/edit/" + id;
        }
    }

    /**
     * Delete diagnosis
     * POST /diagnoses/delete/{id}
     */
    @PostMapping("/delete/{id}")
    public String deleteDiagnosis(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {
        log.info("Deleting diagnosis with ID: {}", id);
        
        try {
            Diagnosis existing = diagnosisService.getDiagnosisById(id);
            if (!hasDoctorAccess(authentication, existing, redirectAttributes)) {
                return "redirect:/diagnoses";
            }

            diagnosisService.deleteDiagnosis(id);
            redirectAttributes.addFlashAttribute("success", "✅ Diagnosis deleted successfully!");
            
        } catch (Exception e) {
            log.error("Error deleting diagnosis", e);
            redirectAttributes.addFlashAttribute("error", "❌ Error deleting diagnosis: " + e.getMessage());
        }
        
        return "redirect:/diagnoses";
    }

    /**
     * Search diagnoses
     * GET /diagnoses/search?query=грип
     */
    @GetMapping("/search")
    public String searchDiagnoses(
            @RequestParam String query,
            Model model,
            Authentication authentication) {
        log.info("Searching diagnoses with query: {}", query);

        List<Diagnosis> diagnoses = diagnosisService.searchDiagnosesByName(query);
        if (isDoctorUser(authentication)) {
            var currentDoctor = findDoctorByEmail(getUserEmail(authentication));
            if (currentDoctor == null) {
                model.addAttribute("diagnoses", List.of());
                model.addAttribute("query", query);
                model.addAttribute("error", "Doctor account is not linked to a doctor record in the database. Please contact admin.");
                return "diagnoses/diagnoses";
            }
            diagnoses = diagnoses.stream()
                    .filter(d -> d.getRecipe() != null && d.getRecipe().getDoctor() != null
                            && d.getRecipe().getDoctor().getId().equals(currentDoctor.getId()))
                    .collect(Collectors.toList());
        }

        model.addAttribute("diagnoses", diagnoses);
        model.addAttribute("query", query);
        
        return "diagnoses/diagnoses";
    }

    private boolean hasDoctorAccess(Authentication authentication, Diagnosis diagnosis, RedirectAttributes redirectAttributes) {
        if (!isDoctorUser(authentication)) {
            return true;
        }

        var currentDoctor = findDoctorByEmail(getUserEmail(authentication));
        if (currentDoctor == null) {
            redirectAttributes.addFlashAttribute("error",
                    "Doctor account is not linked to a doctor record in the database. Please contact admin.");
            return false;
        }

        if (diagnosis.getRecipe() == null || diagnosis.getRecipe().getDoctor() == null ||
                !currentDoctor.getId().equals(diagnosis.getRecipe().getDoctor().getId())) {
            redirectAttributes.addFlashAttribute("error",
                    "⛔ Access Denied: You can only manage your own diagnoses.");
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
