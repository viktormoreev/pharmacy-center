package com.inf.cscb869_pharmacy.doctor.repository;

import com.inf.cscb869_pharmacy.doctor.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByLicenseNumber(String licenseNumber);

    List<Doctor> findByIsPrimaryDoctorTrue();

    List<Doctor> findBySpecialty(String specialty);

    Optional<Doctor> findByEmailIgnoreCase(String email);

    @Query("SELECT d.name, COUNT(c) FROM Doctor d LEFT JOIN Customer c ON c.primaryDoctor = d GROUP BY d.id, d.name")
    List<Object[]> countPatientsByPrimaryDoctor();

    @Query("SELECT d.name, COUNT(r) FROM Doctor d LEFT JOIN Recipe r ON r.doctor = d GROUP BY d.id, d.name")
    List<Object[]> countVisitsByDoctor();
}
