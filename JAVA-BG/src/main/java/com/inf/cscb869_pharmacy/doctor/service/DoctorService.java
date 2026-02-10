package com.inf.cscb869_pharmacy.doctor.service;

import com.inf.cscb869_pharmacy.doctor.entity.Doctor;

import java.util.List;
import java.util.Optional;

public interface DoctorService {
    List<Doctor> getDoctors();

    Doctor getDoctor(long id);

    Doctor createDoctor(Doctor doctor);

    Doctor updateDoctor(Doctor doctor, long id);

    void deleteDoctor(long id);
    
    long countDoctors();

    Optional<Doctor> findByEmail(String email);
}
