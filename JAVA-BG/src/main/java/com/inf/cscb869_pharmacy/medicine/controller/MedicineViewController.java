package com.inf.cscb869_pharmacy.medicine.controller;

import com.inf.cscb869_pharmacy.medicine.entity.Medicine;
import com.inf.cscb869_pharmacy.medicine.dto.CreateMedicineDTO;
import com.inf.cscb869_pharmacy.medicine.dto.MedicineDTO;
import com.inf.cscb869_pharmacy.medicine.service.MedicineService;
import com.inf.cscb869_pharmacy.util.MapperUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/medicines")
public class MedicineViewController {
    private final MedicineService medicineService;
    private final MapperUtil mapperUtil;

    @GetMapping
    public String getMedicines(Model model) {
        List<MedicineDTO> medicines = mapperUtil
                .mapList(this.medicineService.getMedicines(), MedicineDTO.class);
        model.addAttribute("medicines", medicines);
        return "/medicines/medicines.html";
    }

    @GetMapping("/create-medicine")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateMedicineForm(Model model) {
        model.addAttribute("medicine", new CreateMedicineDTO());
        return "/medicines/create-medicine";
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createMedicine(@Valid @ModelAttribute("medicine") CreateMedicineDTO medicine, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "/medicines/create-medicine";
        }
        this.medicineService.createMedicine(medicine);
        return "redirect:/medicines";
    }

    @GetMapping("/edit-medicine/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditMedicineForm(Model model, @PathVariable Long id) {
        MedicineDTO medicineDTO = this.medicineService.getMedicine(id);
        model.addAttribute("medicine", medicineDTO);
        return "/medicines/edit-medicine";
    }

    @PostMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateMedicine(@PathVariable long id, @Valid @ModelAttribute("medicine") MedicineDTO medicineDTO, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "/medicines/edit-medicine";
        }
        
        // Convert DTO to Entity
        Medicine medicine = mapperUtil.getModelMapper().map(medicineDTO, Medicine.class);
        this.medicineService.updateMedicine(medicine, id);
        return "redirect:/medicines";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteMedicine(@PathVariable int id) {
        this.medicineService.deleteMedicine(id);
        return "redirect:/medicines";
    }


}


