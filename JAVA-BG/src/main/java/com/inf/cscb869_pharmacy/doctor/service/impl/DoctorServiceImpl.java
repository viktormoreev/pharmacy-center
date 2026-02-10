package com.inf.cscb869_pharmacy.doctor.service.impl;

import com.inf.cscb869_pharmacy.doctor.entity.Doctor;
import com.inf.cscb869_pharmacy.doctor.repository.DoctorRepository;
import com.inf.cscb869_pharmacy.doctor.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;

    @Override
    public List<Doctor> getDoctors() {
        return this.doctorRepository.findAll();
    }

    @Override
    public Doctor getDoctor(long id) {
        return this.doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor with id=" + id + " not found!" ));
    }

    @Override
    public Doctor createDoctor(Doctor doctor) {
        return this.doctorRepository.save(doctor);
    }

    @Override
    public Doctor updateDoctor(Doctor doctor, long id) {
        return this.doctorRepository.findById(id)
                .map(doctor1 -> {
                    doctor1.setName(doctor.getName());
                    return this.doctorRepository.save(doctor1);
                }).orElseGet(()->
                        this.doctorRepository.save(doctor)
                );
    }

    @Override
    public void deleteDoctor(long id) {
        this.doctorRepository.deleteById(id);
    }

    @Override
    public long countDoctors() {
        return this.doctorRepository.count();
    }

    @Override
    public Optional<Doctor> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return doctorRepository.findByEmailIgnoreCase(email);
    }
}
