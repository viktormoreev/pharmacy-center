package com.inf.cscb869_pharmacy.sickleave.entity;

import com.inf.cscb869_pharmacy.common.BaseEntity;
import com.inf.cscb869_pharmacy.customer.entity.Customer;
import com.inf.cscb869_pharmacy.doctor.entity.Doctor;
import com.inf.cscb869_pharmacy.recipe.entity.Recipe;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Sick Leave entity - represents a medical sick leave certificate
 * Болничен лист издаден от лекар
 */
@Entity
@Table(name = "sick_leaves")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SickLeave extends BaseEntity {

    /**
     * Unique sick leave number
     * Уникален номер на болничния
     */
    @Column(name = "leave_number", unique = true, length = 50)
    private String leaveNumber;

    /**
     * The recipe/examination that issued this sick leave
     * Прегледът, при който е издаден болничния
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    @NotNull(message = "Recipe is required")
    private Recipe recipe;

    /**
     * Patient who received the sick leave
     * Пациент, на който е издаден болничния
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @NotNull(message = "Patient is required")
    private Customer customer;

    /**
     * Doctor who issued the sick leave
     * Лекар, който е издал болничния
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    @NotNull(message = "Doctor is required")
    private Doctor doctor;

    /**
     * Start date of sick leave
     * Начална дата на болничния
     */
    @NotNull(message = "Start date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * Number of days
     * Брой дни
     */
    @NotNull(message = "Duration in days is required")
    @Min(value = 1, message = "Duration must be at least 1 day")
    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    /**
     * End date of sick leave (calculated)
     * Крайна дата на болничния
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * Reason for sick leave
     * Причина за болничния
     */
    @Column(length = 1000)
    private String reason;

    /**
     * Status of the sick leave
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Builder.Default
    private SickLeaveStatus status = SickLeaveStatus.ACTIVE;

    /**
     * Date when sick leave was issued
     * Дата на издаване
     */
    @NotNull(message = "Issue date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    /**
     * Additional notes
     */
    @Column(length = 1000)
    private String notes;

    /**
     * Calculate and set end date based on start date and duration
     */
    @PrePersist
    @PreUpdate
    public void calculateEndDate() {
        if (startDate != null && durationDays != null) {
            this.endDate = startDate.plusDays(durationDays - 1); // -1 because start day counts
        }
    }

    /**
     * Check if sick leave is currently active
     */
    public boolean isCurrentlyActive() {
        LocalDate today = LocalDate.now();
        return status == SickLeaveStatus.ACTIVE &&
               !today.isBefore(startDate) &&
               !today.isAfter(endDate);
    }

    /**
     * Check if sick leave has expired
     */
    public boolean isExpired() {
        return endDate != null && LocalDate.now().isAfter(endDate);
    }

    @Override
    public String toString() {
        return "SickLeave{" +
                "id=" + getId() +
                ", leaveNumber='" + leaveNumber + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", durationDays=" + durationDays +
                ", status=" + status +
                '}';
    }
}
