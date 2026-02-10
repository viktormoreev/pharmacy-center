package com.inf.cscb869_pharmacy.customer.repository;

import com.inf.cscb869_pharmacy.customer.entity.Customer;
import com.inf.cscb869_pharmacy.doctor.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Find customer by email
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Find customer by EGN (Bulgarian National ID)
     */
    Optional<Customer> findByEgn(String egn);

    /**
     * Find customers by name (case-insensitive)
     */
    List<Customer> findByNameContainingIgnoreCase(String name);

    /**
     * Find all active customers
     */
    List<Customer> findByActiveTrue();

    /**
     * Find customers by age range
     */
    List<Customer> findByAgeBetween(Integer minAge, Integer maxAge);

    /**
     * Find customers with specific allergies
     */
    @Query("SELECT c FROM Customer c WHERE LOWER(c.allergies) LIKE LOWER(CONCAT('%', :allergy, '%'))")
    List<Customer> findByAllergiesContaining(@Param("allergy") String allergy);

    /**
     * Find customers with insurance number
     */
    Optional<Customer> findByInsuranceNumber(String insuranceNumber);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find customers with valid insurance (paid within last 6 months)
     */
    @Query("SELECT c FROM Customer c WHERE c.insurancePaidUntil >= :sixMonthsAgo")
    List<Customer> findWithValidInsurance(@Param("sixMonthsAgo") LocalDate sixMonthsAgo);

    /**
     * Find customers without valid insurance
     */
    @Query("SELECT c FROM Customer c WHERE c.insurancePaidUntil < :sixMonthsAgo OR c.insurancePaidUntil IS NULL")
    List<Customer> findWithoutValidInsurance(@Param("sixMonthsAgo") LocalDate sixMonthsAgo);

    /**
     * Find all patients by primary doctor
     */
    List<Customer> findByPrimaryDoctor(Doctor doctor);

    /**
     * Find all patients by primary doctor ID
     */
    List<Customer> findByPrimaryDoctorId(Long doctorId);

    /**
     * Count patients by primary doctor ID
     */
    long countByPrimaryDoctorId(Long doctorId);
}
