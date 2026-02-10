package com.inf.cscb869_pharmacy.doctor.repository;

import com.inf.cscb869_pharmacy.doctor.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    /**
     * Find doctor by license number (УИН)
     */
    Optional<Doctor> findByLicenseNumber(String licenseNumber);

    /**
     * Find all doctors who can be primary doctors
     */
    List<Doctor> findByIsPrimaryDoctorTrue();

    /**
     * Find doctors by specialty
     */
    List<Doctor> findBySpecialty(String specialty);

    /**
     * Find doctor by email (used to map authenticated Keycloak user to doctor record)
     */
    Optional<Doctor> findByEmailIgnoreCase(String email);

    /**
     * Get count of patients per primary doctor
     */
    @Query("SELECT d.name, COUNT(c) FROM Doctor d LEFT JOIN Customer c ON c.primaryDoctor = d GROUP BY d.id, d.name")
    List<Object[]> countPatientsByPrimaryDoctor();

    /**
     * Get count of visits (examinations) per doctor
     */
    @Query("SELECT d.name, COUNT(r) FROM Doctor d LEFT JOIN Recipe r ON r.doctor = d GROUP BY d.id, d.name")
    List<Object[]> countVisitsByDoctor();
}
