package com.inf.cscb869_pharmacy.medicine.service;

import com.inf.cscb869_pharmacy.medicine.entity.Medicine;
import com.inf.cscb869_pharmacy.medicine.dto.CreateMedicineDTO;
import com.inf.cscb869_pharmacy.medicine.dto.MedicineDTO;

import java.util.List;

public interface MedicineService {
    List<MedicineDTO> getMedicines();
    MedicineDTO getMedicine(long id);
    CreateMedicineDTO createMedicine(CreateMedicineDTO medicine);
    Medicine updateMedicine(Medicine medicine, long id);
    void deleteMedicine(long id);
    List<Medicine> getMedicinesByName(String name);
    List<Medicine> getMedicinesByNameStartsWith(String name);
    List<Medicine> getMedicinesByNameStartsWithAndAgeAppropriatenessGreaterThan(String name, int age);
    List<Medicine> getMedicinesByAgeAppropriatenessGreaterThanAndNeedsRecipe(int age, boolean needsRecipe);
    long countMedicines();
    long countMedicinesNeedingRecipe();
}
