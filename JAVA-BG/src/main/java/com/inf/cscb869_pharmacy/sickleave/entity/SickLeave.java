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

    @Column(name = "leave_number", unique = true, length = 50)
    private String leaveNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    @NotNull(message = "Recipe is required")
    private Recipe recipe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @NotNull(message = "Patient is required")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    @NotNull(message = "Doctor is required")
    private Doctor doctor;

    @NotNull(message = "Start date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "Duration in days is required")
    @Min(value = 1, message = "Duration must be at least 1 day")
    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(length = 1000)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Builder.Default
    private SickLeaveStatus status = SickLeaveStatus.ACTIVE;

    @NotNull(message = "Issue date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(length = 1000)
    private String notes;

    @PrePersist
    @PreUpdate
    public void calculateEndDate() {
        if (startDate != null && durationDays != null) {
            this.endDate = startDate.plusDays(durationDays - 1); // -1 because start day counts
        }
    }

    public boolean isCurrentlyActive() {
        LocalDate today = LocalDate.now();
        return status == SickLeaveStatus.ACTIVE &&
               !today.isBefore(startDate) &&
               !today.isAfter(endDate);
    }

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
