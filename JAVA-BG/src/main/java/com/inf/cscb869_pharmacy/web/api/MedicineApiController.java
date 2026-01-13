package com.inf.cscb869_pharmacy.web.api;

import com.inf.cscb869_pharmacy.data.entity.Medicine;
import com.inf.cscb869_pharmacy.dto.CreateMedicineDTO;
import com.inf.cscb869_pharmacy.dto.MedicineDTO;
import com.inf.cscb869_pharmacy.service.MedicineService;
import com.inf.cscb869_pharmacy.util.MapperUtil;
import com.inf.cscb869_pharmacy.web.view.controller.model.CreateMedicineViewModel;
import com.inf.cscb869_pharmacy.web.view.controller.model.MedicineViewModel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/medicines")
public class MedicineApiController {
    private final MedicineService medicineService;
    private final MapperUtil mapperUtil;
    @GetMapping
    public List<MedicineDTO> getMedicines() {

        return this.medicineService.getMedicines();
    }
    @GetMapping("/{id}")
    public MedicineViewModel getMedicine(@PathVariable long id){
        return mapperUtil.getModelMapper().map(this.medicineService.getMedicine(id), MedicineViewModel.class);
    }

    @PostMapping
    public CreateMedicineViewModel createMedicine(@RequestBody CreateMedicineViewModel medicine) {
        return mapperUtil.getModelMapper().map(this.medicineService
                .createMedicine(mapperUtil.getModelMapper().map(medicine, CreateMedicineDTO.class)), CreateMedicineViewModel.class);
    }

    @PutMapping("/{id}")
    public Medicine updateMedicine(@RequestBody Medicine medicine,@PathVariable long id) {
        return this.medicineService.updateMedicine(medicine, id);
    }

    @DeleteMapping("/{id}")
    public void deleteMedicine(@PathVariable long id) {
        this.medicineService.deleteMedicine(id);
    }

    @GetMapping("/by-name/{name}")
    public List<Medicine> getMedicinesByName(@PathVariable String name) {
        return this.medicineService.getMedicinesByName(name);
    }

    @GetMapping("/by-name-starts-with/{name}")
    public List<Medicine> getMedicinesByNameStartsWith(@PathVariable String name) {
        return this.medicineService.getMedicinesByNameStartsWith(name);
    }

    @GetMapping("/by-name/{name}/age-appropriateness/{age}")
    public List<Medicine> getMedicinesByNameStartsWithAndAgeAppropriatenessGreaterThan(@PathVariable String name, @PathVariable int age) {
        return this.medicineService.getMedicinesByNameStartsWithAndAgeAppropriatenessGreaterThan(name, age);
    }

    @GetMapping("/age-appropriateness/{age}/needs-recipe/{needsRecipe}")
    public List<Medicine> getMedicinesByAgeAppropriatenessGreaterThanAndNeedsRecipe(@PathVariable int age, @PathVariable boolean needsRecipe) {
        return this.medicineService.getMedicinesByAgeAppropriatenessGreaterThanAndNeedsRecipe(age, needsRecipe);
    }
}


