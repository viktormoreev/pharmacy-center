package com.inf.cscb869_pharmacy.web.view.controller;

import com.inf.cscb869_pharmacy.data.entity.Medicine;
import com.inf.cscb869_pharmacy.dto.CreateMedicineDTO;
import com.inf.cscb869_pharmacy.service.MedicineService;
import com.inf.cscb869_pharmacy.util.MapperUtil;
import com.inf.cscb869_pharmacy.web.view.controller.model.CreateMedicineViewModel;
import com.inf.cscb869_pharmacy.web.view.controller.model.MedicineViewModel;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
        List<MedicineViewModel> medicines = mapperUtil
                .mapList(this.medicineService.getMedicines(), MedicineViewModel.class);
        model.addAttribute("medicines", medicines);
        return "/medicines/medicines.html";
    }

    @GetMapping("/create-medicine")
    public String showCreateMedicineForm(Model model) {
        model.addAttribute("medicine", new CreateMedicineViewModel());
        return "/medicines/create-medicine";
    }

    @PostMapping("/create")
    public String createMedicine(@Valid @ModelAttribute("medicine") CreateMedicineViewModel medicine, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "/medicines/create-medicine";
        }
        this.medicineService
                .createMedicine(mapperUtil.getModelMapper().map(medicine, CreateMedicineDTO.class));
        return "redirect:/medicines";
    }

    @GetMapping("/edit-medicine/{id}")
    public String showEditMedicineForm(Model model, @PathVariable Long id) {
        model.addAttribute("medicine", this.medicineService.getMedicine(id));
        return "/medicines/edit-medicine";
    }

    @PostMapping("/update/{id}")
    public String updateMedicine(@PathVariable long id, Medicine medicine) {
        this.medicineService.updateMedicine(medicine, id);
        return "redirect:/medicines";
    }

    @GetMapping("/delete/{id}")
    public String deleteMedicine(@PathVariable int id) {
        this.medicineService.deleteMedicine(id);
        return "redirect:/medicines";
    }


}



