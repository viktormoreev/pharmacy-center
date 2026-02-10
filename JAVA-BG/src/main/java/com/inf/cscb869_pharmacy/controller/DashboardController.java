package com.inf.cscb869_pharmacy.controller;

import com.inf.cscb869_pharmacy.customer.service.CustomerService;
import com.inf.cscb869_pharmacy.diagnosis.service.DiagnosisService;
import com.inf.cscb869_pharmacy.doctor.service.DoctorService;
import com.inf.cscb869_pharmacy.medicine.service.MedicineService;
import com.inf.cscb869_pharmacy.recipe.entity.Recipe;
import com.inf.cscb869_pharmacy.recipe.service.RecipeService;
import com.inf.cscb869_pharmacy.sickleave.entity.SickLeaveStatus;
import com.inf.cscb869_pharmacy.sickleave.service.SickLeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final MedicineService medicineService;
    private final RecipeService recipeService;
    private final CustomerService customerService;
    private final DoctorService doctorService;
    private final DiagnosisService diagnosisService;
    private final SickLeaveService sickLeaveService;

    @GetMapping
    public String showDashboard(Model model) {
        // Statistics
        model.addAttribute("totalMedicines", medicineService.countMedicines());
        model.addAttribute("totalRecipes", recipeService.countRecipes());
        model.addAttribute("totalCustomers", customerService.countCustomers());
        model.addAttribute("totalDoctors", doctorService.countDoctors());
        model.addAttribute("medicinesNeedingRecipe", medicineService.countMedicinesNeedingRecipe());
        model.addAttribute("activeCustomers", customerService.countActiveCustomers());
        
        // Medical Records Statistics
        try {
            model.addAttribute("totalDiagnoses", diagnosisService.getAllDiagnoses().size());
            model.addAttribute("primaryDiagnoses", diagnosisService.getPrimaryDiagnoses().size());
            model.addAttribute("totalSickLeaves", sickLeaveService.getAllSickLeaves().size());
            model.addAttribute("activeSickLeaves", sickLeaveService.getSickLeavesByStatus(SickLeaveStatus.ACTIVE).size());
        } catch (Exception e) {
            // If tables don't exist yet, set defaults
            model.addAttribute("totalDiagnoses", 0);
            model.addAttribute("primaryDiagnoses", 0);
            model.addAttribute("totalSickLeaves", 0);
            model.addAttribute("activeSickLeaves", 0);
        }
        
        // Recipe status counts for charts
        model.addAttribute("activeRecipes", recipeService.countRecipesByStatus("ACTIVE"));
        model.addAttribute("fulfilledRecipes", recipeService.countRecipesByStatus("FULFILLED"));
        model.addAttribute("expiredRecipes", recipeService.countRecipesByStatus("EXPIRED"));
        model.addAttribute("cancelledRecipes", recipeService.countRecipesByStatus("CANCELLED"));
        
        // Recent recipes (last 5)
        List<Recipe> recentRecipes = recipeService.getRecipes().stream()
                .sorted(Comparator.comparing(Recipe::getCreationDate).reversed())
                .limit(5)
                .collect(Collectors.toList());
        model.addAttribute("recentRecipes", recentRecipes);
        
        return "dashboard";
    }
}
