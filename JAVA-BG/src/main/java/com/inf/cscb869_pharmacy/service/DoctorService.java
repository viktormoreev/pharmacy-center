package com.inf.cscb869_pharmacy.service;

import com.inf.cscb869_pharmacy.data.entity.Doctor;

import java.util.List;

public interface DoctorService {
    List<Doctor> getDoctors();

    Doctor getDoctor(long id);

    Doctor createDoctor(Doctor doctor);

    Doctor updateDoctor(Doctor doctor, long id);

    void deleteDoctor(long id);
}
