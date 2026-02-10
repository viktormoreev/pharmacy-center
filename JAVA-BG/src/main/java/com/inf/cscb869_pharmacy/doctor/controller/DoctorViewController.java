package com.inf.cscb869_pharmacy.doctor.controller;

import com.inf.cscb869_pharmacy.doctor.entity.Doctor;
import com.inf.cscb869_pharmacy.doctor.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MVC Controller for Doctor Management UI
 */
@Controller
@RequestMapping("/doctors")
@RequiredArgsConstructor
public class DoctorViewController {

    private final DoctorService doctorService;

    /**
     * List all doctors
     */
    @GetMapping
    public String listDoctors(Model model) {
        model.addAttribute("doctors", doctorService.getDoctors());
        return "doctors/doctors";
    }

    /**
     * Show create doctor form
     */
    @GetMapping("/create")
    @PreAuthorize("hasAnyRole('PHARMACIST','ADMIN')")
    public String showCreateForm(Model model) {
        model.addAttribute("doctor", new Doctor());
        return "doctors/create-doctor";
    }

    /**
     * Handle create doctor form submission
     */
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('PHARMACIST','ADMIN')")
    public String createDoctor(@Valid @ModelAttribute("doctor") Doctor doctor,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "doctors/create-doctor";
        }

        doctorService.createDoctor(doctor);
        redirectAttributes.addFlashAttribute("successMessage", "Doctor created successfully!");
        return "redirect:/doctors";
    }

    /**
     * Show doctor details (read-only)
     */
    @GetMapping("/{id}")
    public String viewDoctor(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Doctor doctor = doctorService.getDoctor(id);
            model.addAttribute("doctor", doctor);
            return "doctors/view-doctor";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Doctor not found!");
            return "redirect:/doctors";
        }
    }

    /**
     * Show edit doctor form
     */
    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('PHARMACIST','ADMIN')")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Doctor doctor = doctorService.getDoctor(id);
            model.addAttribute("doctor", doctor);
            return "doctors/edit-doctor";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Doctor not found!");
            return "redirect:/doctors";
        }
    }

    /**
     * Handle edit doctor form submission
     */
    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('PHARMACIST','ADMIN')")
    public String updateDoctor(@PathVariable Long id,
                               @Valid @ModelAttribute("doctor") Doctor doctor,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "doctors/edit-doctor";
        }

        doctorService.updateDoctor(doctor, id);
        redirectAttributes.addFlashAttribute("successMessage", "Doctor updated successfully!");
        return "redirect:/doctors";
    }

    /**
     * Delete doctor
     */
    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('PHARMACIST','ADMIN')")
    public String deleteDoctor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            doctorService.deleteDoctor(id);
            redirectAttributes.addFlashAttribute("successMessage", "Doctor deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete doctor: " + e.getMessage());
        }
        return "redirect:/doctors";
    }
}
