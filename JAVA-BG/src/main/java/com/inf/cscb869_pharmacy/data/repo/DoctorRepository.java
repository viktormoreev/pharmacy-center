package com.inf.cscb869_pharmacy.data.repo;

import com.inf.cscb869_pharmacy.data.entity.Doctor;
import com.inf.cscb869_pharmacy.data.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
}
