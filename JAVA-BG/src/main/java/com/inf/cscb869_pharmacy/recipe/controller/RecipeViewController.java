package com.inf.cscb869_pharmacy.recipe.controller;

import com.inf.cscb869_pharmacy.customer.service.CustomerService;
import com.inf.cscb869_pharmacy.diagnosis.entity.Diagnosis;
import com.inf.cscb869_pharmacy.diagnosis.service.DiagnosisService;
import com.inf.cscb869_pharmacy.doctor.service.DoctorService;
import com.inf.cscb869_pharmacy.medicine.entity.Medicine;
import com.inf.cscb869_pharmacy.medicine.repository.MedicineRepository;
import com.inf.cscb869_pharmacy.medicine.service.MedicineService;
import com.inf.cscb869_pharmacy.recipe.dto.RecipeDTO;
import com.inf.cscb869_pharmacy.recipe.dto.RecipeMedicineDTO;
import com.inf.cscb869_pharmacy.recipe.entity.Recipe;
import com.inf.cscb869_pharmacy.recipe.entity.RecipeMedicine;
import com.inf.cscb869_pharmacy.recipe.entity.RecipeStatus;
import com.inf.cscb869_pharmacy.recipe.service.RecipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * View Controller for Recipe/Prescription Management UI
 */
@Controller
@RequestMapping("/recipes")
@RequiredArgsConstructor
public class RecipeViewController {

    private final RecipeService recipeService;
    private final DoctorService doctorService;
    private final CustomerService customerService;
    private final DiagnosisService diagnosisService;
    private final MedicineService medicineService;
    private final MedicineRepository medicineRepository;

    /**
     * List all recipes
     */
    @GetMapping
    public String listRecipes(@RequestParam(required = false) String status, Model model, Authentication authentication) {
        var recipes = recipeService.getRecipes();
        
        // Check if user is a DOCTOR - if so, only show their prescriptions
        String userEmail = getUserEmail(authentication);
        var doctor = findDoctorByEmail(userEmail);
        if (isDoctorUser(authentication) && doctor == null) {
            recipes = new ArrayList<>();
            model.addAttribute("doctorMappingMissing", true);
            model.addAttribute("error", "Doctor account is not linked to a doctor record in the database. Please contact admin.");
            model.addAttribute("isDoctorUser", true);
        } else if (isDoctorUser(authentication) && doctor != null) {
            // Filter to only show this doctor's prescriptions
            recipes = recipes.stream()
                    .filter(r -> r.getDoctor() != null
                            && r.getDoctor().getId() != null
                            && doctor.getId() != null
                            && r.getDoctor().getId().equals(doctor.getId()))
                    .collect(Collectors.toList());
            model.addAttribute("isDoctorUser", true);
        } else {
            model.addAttribute("isDoctorUser", false);
        }
        
        // Filter by status if provided
        if (status != null && !status.trim().isEmpty()) {
            try {
                RecipeStatus filterStatus = RecipeStatus.valueOf(status.toUpperCase());
                recipes = recipes.stream()
                        .filter(r -> r.getStatus() == filterStatus)
                        .collect(Collectors.toList());
                model.addAttribute("selectedStatus", status);
            } catch (IllegalArgumentException e) {
                // Invalid status, show all
            }
        }
        
        model.addAttribute("recipes", recipes);
        model.addAttribute("statuses", RecipeStatus.values());
        return "recipes/recipes";
    }

    /**
     * View recipe details
     */
    @GetMapping("/{id}")
    public String viewRecipe(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Recipe recipe = recipeService.getRecipe(id);
            model.addAttribute("recipe", recipe);
            return "recipes/view-recipe";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Recipe not found: " + e.getMessage());
            return "redirect:/recipes";
        }
    }

    /**
     * Show create recipe form
     */
    @GetMapping("/create")
    public String showCreateForm(Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        RecipeDTO recipeDTO = RecipeDTO.builder()
                .status(RecipeStatus.ACTIVE)
                .medicines(new ArrayList<>())
                .build();

        // Check if user is a DOCTOR - if so, auto-assign them
        String userEmail = getUserEmail(authentication);
        var doctor = findDoctorByEmail(userEmail);
        if (isDoctorUser(authentication) && doctor == null) {
            redirectAttributes.addFlashAttribute("error",
                    "Doctor account is not linked to a doctor record in the database. Please contact admin.");
            return "redirect:/recipes";
        } else if (doctor != null) {
            recipeDTO.setDoctorId(doctor.getId());
            model.addAttribute("isDoctorUser", true);
            model.addAttribute("currentDoctor", doctor);
        } else {
            model.addAttribute("isDoctorUser", false);
        }
        
        model.addAttribute("recipeDTO", recipeDTO);
        model.addAttribute("doctors", doctorService.getDoctors());
        model.addAttribute("customers", customerService.getActiveCustomers());
        model.addAttribute("diagnosisOptions", getDiagnosisOptions());
        model.addAttribute("medicines", medicineService.getMedicines());
        model.addAttribute("statuses", RecipeStatus.values());
        
        return "recipes/create-recipe";
    }

    /**
     * Handle create recipe form submission
     */
    @PostMapping("/create")
    public String createRecipe(@Valid @ModelAttribute("recipeDTO") RecipeDTO recipeDTO,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes,
                               Authentication authentication) {
        if (isDoctorUser(authentication)) {
            String userEmail = getUserEmail(authentication);
            var doctor = findDoctorByEmail(userEmail);
            if (doctor == null) {
                redirectAttributes.addFlashAttribute("error",
                        "Doctor account is not linked to a doctor record in the database. Please contact admin.");
                return "redirect:/recipes";
            }
            // Prevent tampering with doctorId in the form.
            recipeDTO.setDoctorId(doctor.getId());
        }

        recipeDTO.setDiagnosis(combineDiagnoses(recipeDTO.getSelectedDiagnoses(), recipeDTO.getDiagnosis()));

        if (bindingResult.hasErrors()) {
            model.addAttribute("doctors", doctorService.getDoctors());
            model.addAttribute("customers", customerService.getActiveCustomers());
            model.addAttribute("diagnosisOptions", getDiagnosisOptions());
            model.addAttribute("medicines", medicineService.getMedicines());
            model.addAttribute("statuses", RecipeStatus.values());
            return "recipes/create-recipe";
        }

        try {
            Recipe recipe = convertToEntity(recipeDTO);
            RecipeDTO savedRecipe = recipeService.createRecipe(recipe);
            redirectAttributes.addFlashAttribute("success", "Recipe created successfully!");
            return "redirect:/recipes/" + savedRecipe.getId();
        } catch (Exception e) {
            model.addAttribute("error", "Error creating recipe: " + e.getMessage());
            model.addAttribute("doctors", doctorService.getDoctors());
            model.addAttribute("customers", customerService.getActiveCustomers());
            model.addAttribute("diagnosisOptions", getDiagnosisOptions());
            model.addAttribute("medicines", medicineService.getMedicines());
            model.addAttribute("statuses", RecipeStatus.values());
            return "recipes/create-recipe";
        }
    }

    /**
     * Show edit recipe form
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes, Authentication authentication) {
        try {
            Recipe recipe = recipeService.getRecipe(id);
            
            // ✅ НОВО: Check if user is a DOCTOR - if so, they can only edit their own prescriptions
            String userEmail = getUserEmail(authentication);
            var doctor = findDoctorByEmail(userEmail);
            if (isDoctorUser(authentication) && doctor == null) {
                redirectAttributes.addFlashAttribute("error",
                        "Doctor account is not linked to a doctor record in the database. Please contact admin.");
                return "redirect:/recipes";
            } else if (doctor != null) {
                // This is a doctor user - check if they own this prescription
                if (!recipe.getDoctor().getId().equals(doctor.getId())) {
                    redirectAttributes.addFlashAttribute("error", 
                        "⛔ Access Denied: You can only edit your own prescriptions!");
                    return "redirect:/recipes";
                }
                model.addAttribute("isDoctorUser", true);
                model.addAttribute("currentDoctor", recipe.getDoctor());
            } else {
                model.addAttribute("isDoctorUser", false);
            }
            
            RecipeDTO recipeDTO = convertToDTO(recipe);
            model.addAttribute("recipeDTO", recipeDTO);
            model.addAttribute("doctors", doctorService.getDoctors());
            model.addAttribute("customers", customerService.getActiveCustomers());
            model.addAttribute("diagnosisOptions", getDiagnosisOptions());
            model.addAttribute("medicines", medicineService.getMedicines());
            model.addAttribute("statuses", RecipeStatus.values());
            
            return "recipes/edit-recipe";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Recipe not found: " + e.getMessage());
            return "redirect:/recipes";
        }
    }

    /**
     * Handle update recipe
     */
    @PostMapping("/edit/{id}")
    public String updateRecipe(@PathVariable Long id,
                               @Valid @ModelAttribute("recipeDTO") RecipeDTO recipeDTO,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes,
                               Authentication authentication) {
        
        // ✅ НОВО: Security check - doctors can only edit their own prescriptions
        try {
            Recipe existingRecipe = recipeService.getRecipe(id);
            String userEmail = getUserEmail(authentication);
            var doctor = findDoctorByEmail(userEmail);
            
            if (isDoctorUser(authentication) && doctor == null) {
                redirectAttributes.addFlashAttribute("error",
                        "Doctor account is not linked to a doctor record in the database. Please contact admin.");
                return "redirect:/recipes";
            }

            if (doctor != null && !existingRecipe.getDoctor().getId().equals(doctor.getId())) {
                redirectAttributes.addFlashAttribute("error",
                        "⛔ Access Denied: You can only edit your own prescriptions!");
                return "redirect:/recipes";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/recipes";
        }

        recipeDTO.setDiagnosis(combineDiagnoses(recipeDTO.getSelectedDiagnoses(), recipeDTO.getDiagnosis()));
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("doctors", doctorService.getDoctors());
            model.addAttribute("customers", customerService.getActiveCustomers());
            model.addAttribute("diagnosisOptions", getDiagnosisOptions());
            model.addAttribute("medicines", medicineService.getMedicines());
            model.addAttribute("statuses", RecipeStatus.values());
            return "recipes/edit-recipe";
        }

        try {
            Recipe recipe = convertToEntity(recipeDTO);
            RecipeDTO updatedRecipe = recipeService.updateRecipe(recipe, id);
            redirectAttributes.addFlashAttribute("success", "Recipe updated successfully!");
            return "redirect:/recipes/" + updatedRecipe.getId();
        } catch (Exception e) {
            model.addAttribute("error", "Error updating recipe: " + e.getMessage());
            model.addAttribute("doctors", doctorService.getDoctors());
            model.addAttribute("customers", customerService.getActiveCustomers());
            model.addAttribute("diagnosisOptions", getDiagnosisOptions());
            model.addAttribute("medicines", medicineService.getMedicines());
            model.addAttribute("statuses", RecipeStatus.values());
            return "recipes/edit-recipe";
        }
    }

    /**
     * Delete recipe
     */
    @PostMapping("/delete/{id}")
    public String deleteRecipe(@PathVariable Long id, RedirectAttributes redirectAttributes, Authentication authentication) {
        try {
            // ✅ НОВО: Security check - doctors can only delete their own prescriptions
            Recipe recipe = recipeService.getRecipe(id);
            String userEmail = getUserEmail(authentication);
            var doctor = findDoctorByEmail(userEmail);
            
            if (isDoctorUser(authentication) && doctor == null) {
                redirectAttributes.addFlashAttribute("error",
                        "Doctor account is not linked to a doctor record in the database. Please contact admin.");
                return "redirect:/recipes";
            }

            if (doctor != null && !recipe.getDoctor().getId().equals(doctor.getId())) {
                redirectAttributes.addFlashAttribute("error",
                        "⛔ Access Denied: You can only delete your own prescriptions!");
                return "redirect:/recipes";
            }
            
            recipeService.deleteRecipe(id);
            redirectAttributes.addFlashAttribute("success", "Recipe deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting recipe: " + e.getMessage());
        }
        return "redirect:/recipes";
    }

    /**
     * Convert DTO to Entity
     */
    private Recipe convertToEntity(RecipeDTO dto) {
        var doctor = doctorService.getDoctor(dto.getDoctorId());
        var customer = customerService.getCustomerById(dto.getCustomerId());
        String combinedDiagnosis = combineDiagnoses(dto.getSelectedDiagnoses(), dto.getDiagnosis());
        
        Recipe recipe = Recipe.builder()
                .creationDate(dto.getCreationDate())
                .doctor(doctor)
                .customer(customer)
                .status(dto.getStatus())
                .diagnosis(combinedDiagnosis)
                .notes(dto.getNotes())
                .expirationDate(dto.getExpirationDate())
                .recipeMedicines(new ArrayList<>())
                .build();

        List<String> diagnosisNames = splitDiagnosisValues(combinedDiagnosis);
        for (int index = 0; index < diagnosisNames.size(); index++) {
            String diagnosisName = diagnosisNames.get(index);
            if (diagnosisName.length() < 3) {
                continue;
            }
            Diagnosis diagnosis = Diagnosis.builder()
                    .recipe(recipe)
                    .name(diagnosisName)
                    .diagnosisDate(dto.getCreationDate())
                    .isPrimary(index == 0)
                    .build();
            recipe.getDiagnoses().add(diagnosis);
        }

        // Add medicines
        if (dto.getMedicines() != null) {
            for (RecipeMedicineDTO medDTO : dto.getMedicines()) {
                if (medDTO.getMedicineId() != null) {
                    // Get Medicine entity from repository
                    var medicine = medicineRepository.findById(medDTO.getMedicineId())
                            .orElseThrow(() -> new RuntimeException("Medicine not found"));
                    
                    RecipeMedicine recipeMedicine = RecipeMedicine.builder()
                            .recipe(recipe)
                            .medicine(medicine)
                            .dosage(medDTO.getDosage())
                            .durationDays(medDTO.getDurationDays())
                            .instructions(medDTO.getInstructions())
                            .quantity(medDTO.getQuantity())
                            .build();
                    
                    recipe.addMedicine(recipeMedicine);
                }
            }
        }

        return recipe;
    }

    /**
     * Convert Entity to DTO
     */
    private RecipeDTO convertToDTO(Recipe recipe) {
        List<String> selectedDiagnoses = recipe.getDiagnoses() == null
                ? new ArrayList<>()
                : recipe.getDiagnoses().stream()
                .map(Diagnosis::getName)
                .filter(name -> name != null && !name.isBlank())
                .map(String::trim)
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(LinkedHashSet::new),
                        ArrayList::new
                ));
        if (selectedDiagnoses.isEmpty()) {
            selectedDiagnoses = splitDiagnosisValues(recipe.getDiagnosis());
        }

        RecipeDTO dto = RecipeDTO.builder()
                .id(recipe.getId())
                .creationDate(recipe.getCreationDate())
                .doctorId(recipe.getDoctor().getId())
                .customerId(recipe.getCustomer().getId())
                .status(recipe.getStatus())
                .diagnosis("")
                .selectedDiagnoses(selectedDiagnoses)
                .notes(recipe.getNotes())
                .expirationDate(recipe.getExpirationDate())
                .medicines(new ArrayList<>())
                .build();

        // Convert medicines
        if (recipe.getRecipeMedicines() != null) {
            for (RecipeMedicine rm : recipe.getRecipeMedicines()) {
                RecipeMedicineDTO medDTO = RecipeMedicineDTO.builder()
                        .id(rm.getId())
                        .medicineId(rm.getMedicine().getId())
                        .dosage(rm.getDosage())
                        .durationDays(rm.getDurationDays())
                        .instructions(rm.getInstructions())
                        .quantity(rm.getQuantity())
                        .build();
                dto.addMedicine(medDTO);
            }
        }

        return dto;
    }

    /**
     * Get user email from Authentication (Keycloak)
     */
    private String getUserEmail(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof OidcUser) {
            OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
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

    /**
     * Find doctor by email - doctors.email must match Keycloak email claim.
     */
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

    private String combineDiagnoses(List<String> selectedDiagnoses, String freeTextDiagnosis) {
        Map<String, String> uniqueDiagnoses = new LinkedHashMap<>();
        addDiagnoses(uniqueDiagnoses, selectedDiagnoses);
        addDiagnoses(uniqueDiagnoses, splitDiagnosisValues(freeTextDiagnosis));
        if (uniqueDiagnoses.isEmpty()) {
            return null;
        }
        return String.join(", ", uniqueDiagnoses.values());
    }

    private void addDiagnoses(Map<String, String> uniqueDiagnoses, List<String> diagnoses) {
        if (diagnoses == null) {
            return;
        }
        for (String diagnosis : diagnoses) {
            if (diagnosis == null) {
                continue;
            }
            String trimmed = diagnosis.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String key = trimmed.toLowerCase();
            uniqueDiagnoses.putIfAbsent(key, trimmed);
        }
    }

    private List<String> splitDiagnosisValues(String diagnosisText) {
        if (diagnosisText == null || diagnosisText.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.stream(diagnosisText.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<String> getDiagnosisOptions() {
        LinkedHashSet<String> options = new LinkedHashSet<>();

        diagnosisService.getAllDiagnoses().stream()
                .map(d -> d.getName())
                .filter(name -> name != null && !name.isBlank())
                .map(String::trim)
                .forEach(options::add);

        recipeService.getRecipes().stream()
                .map(Recipe::getDiagnosis)
                .filter(name -> name != null && !name.isBlank())
                .flatMap(name -> splitDiagnosisValues(name).stream())
                .forEach(options::add);

        return options.stream()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }
}
