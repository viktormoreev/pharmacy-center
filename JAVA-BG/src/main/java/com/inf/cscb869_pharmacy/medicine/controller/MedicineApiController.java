package com.inf.cscb869_pharmacy.medicine.controller;

import com.inf.cscb869_pharmacy.medicine.entity.Medicine;
import com.inf.cscb869_pharmacy.medicine.dto.CreateMedicineDTO;
import com.inf.cscb869_pharmacy.medicine.dto.MedicineDTO;
import com.inf.cscb869_pharmacy.medicine.service.MedicineService;
import com.inf.cscb869_pharmacy.util.MapperUtil;
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
    public MedicineDTO getMedicine(@PathVariable long id){
        return mapperUtil.getModelMapper().map(this.medicineService.getMedicine(id), MedicineDTO.class);
    }

    @PostMapping
    public CreateMedicineDTO createMedicine(@RequestBody CreateMedicineDTO medicine) {
        return mapperUtil.getModelMapper().map(this.medicineService
                .createMedicine(medicine), CreateMedicineDTO.class);
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


