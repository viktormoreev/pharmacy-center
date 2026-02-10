# 🚀 Quick Start - What to Build Next

## Current Status: 25% Complete ✅

### ✅ DONE (Working Features)
```
✓ Medicine Management
  ✓ Create, Read, Update, Delete medicines
  ✓ Search and filter medicines
  ✓ Beautiful modern UI
  ✓ REST API endpoints
  ✓ Database integration
```

---

## ❌ MISSING - High Priority Tasks

### 1️⃣ CUSTOMER/PATIENT MANAGEMENT (0% - CRITICAL)
**Backend Tasks:**
```java
// Create these files:
├── src/main/java/com/inf/cscb869_pharmacy/customer/
│   ├── entity/Customer.java          // ❌ Create
│   ├── repository/CustomerRepository.java  // ❌ Create
│   ├── service/CustomerService.java        // ❌ Create
│   ├── service/impl/CustomerServiceImpl.java  // ❌ Create
│   ├── controller/CustomerApiController.java  // ❌ Create
│   └── dto/CustomerDTO.java          // ❌ Create

Customer Entity Fields Needed:
- Long id
- String name
- Integer age
- String allergies (or List<String>)
- String phoneNumber
- String email
- Set<Recipe> prescriptions (One-to-Many)
```

**Frontend Tasks:**
```html
<!-- Create these templates: -->
└── src/main/resources/templates/customers/
    ├── customers.html          // ❌ List all customers
    ├── create-customer.html    // ❌ Add new customer form
    └── edit-customer.html      // ❌ Edit customer form

<!-- Add CustomerViewController -->
└── src/main/java/.../controller/CustomerViewController.java  // ❌ Create
```

---

### 2️⃣ FIX RECIPE/PRESCRIPTION SYSTEM (30% - CRITICAL)
**Current Problem:** Recipe only links to Doctor, not to medicines or customers!

**Backend Fixes Needed:**
```java
// Update Recipe.java to include:
@Entity
public class Recipe extends BaseEntity {
    private LocalDate creationDate;
    
    @ManyToOne
    private Doctor doctor;
    
    // ❌ ADD THESE:
    @ManyToOne
    private Customer customer;  // Who is this prescription for?
    
    @ManyToMany
    @JoinTable(name = "recipe_medicines",
        joinColumns = @JoinColumn(name = "recipe_id"),
        inverseJoinColumns = @JoinColumn(name = "medicine_id"))
    private Set<Medicine> medicines;  // What medicines are prescribed?
    
    private String dosage;       // How much to take
    private Integer duration;    // How many days
    private String notes;        // Doctor's notes
    private RecipeStatus status; // ACTIVE, FULFILLED, EXPIRED
}

// Create RecipeStatus enum
public enum RecipeStatus {
    ACTIVE, FULFILLED, EXPIRED, CANCELLED
}
```

---

### 3️⃣ DOCTOR MANAGEMENT UI (0% - HIGH PRIORITY)
**Backend:** ✅ Already exists (DoctorService, DoctorApiController)

**Frontend Needed:**
```bash
# Create these files:
src/main/java/.../controller/DoctorViewController.java  // ❌ Create
src/main/resources/templates/doctors/
  ├── doctors.html              // ❌ List all doctors
  ├── create-doctor.html        // ❌ Add doctor form
  └── edit-doctor.html          // ❌ Edit doctor form
```

**Quick Template Example:**
```html
<!-- doctors.html (similar to medicines.html) -->
<div class="content-card">
    <table class="table custom-table">
        <thead>
            <tr>
                <th>👨‍⚕️ Doctor Name</th>
                <th>📋 Prescriptions</th>
                <th>⚙️ Actions</th>
            </tr>
        </thead>
        <tbody>
            <tr th:each="doctor : ${doctors}">
                <td th:text="${doctor.name}"></td>
                <td th:text="${doctor.recipes?.size() ?: 0}"></td>
                <td>
                    <a class="btn btn-info" th:href="@{'/doctors/edit/'+${doctor.id}}">Edit</a>
                    <a class="btn btn-danger" th:href="@{'/doctors/delete/'+${doctor.id}}">Delete</a>
                </td>
            </tr>
        </tbody>
    </table>
</div>
```

---

### 4️⃣ RECIPE/PRESCRIPTION UI (0% - HIGH PRIORITY)

**Frontend Needed:**
```bash
# Create these files:
src/main/java/.../controller/RecipeViewController.java  // ❌ Create
src/main/resources/templates/recipes/
  ├── recipes.html              // ❌ List all prescriptions
  ├── create-recipe.html        // ❌ Create prescription form
  └── edit-recipe.html          // ❌ Edit prescription form
```

**Create Recipe Form Should Have:**
```html
<form th:action="@{/recipes/create}" method="post">
    <!-- Select Doctor -->
    <select name="doctorId" class="form-control">
        <option th:each="doctor : ${doctors}" 
                th:value="${doctor.id}" 
                th:text="${doctor.name}">
        </option>
    </select>
    
    <!-- Select Customer/Patient -->
    <select name="customerId" class="form-control">
        <option th:each="customer : ${customers}" 
                th:value="${customer.id}" 
                th:text="${customer.name}">
        </option>
    </select>
    
    <!-- Select Medicines (Multi-select) -->
    <select name="medicineIds" class="form-control" multiple>
        <option th:each="medicine : ${medicines}" 
                th:value="${medicine.id}" 
                th:text="${medicine.name}">
        </option>
    </select>
    
    <!-- Date -->
    <input type="date" name="creationDate" class="form-control"/>
    
    <!-- Dosage & Duration -->
    <input type="text" name="dosage" placeholder="e.g., 2 pills daily"/>
    <input type="number" name="duration" placeholder="Duration in days"/>
    
    <button type="submit" class="btn btn-success">Create Prescription</button>
</form>
```

---

### 5️⃣ USER AUTHENTICATION (0% - HIGH PRIORITY)

**Why Critical:** Currently NO login! Anyone can access everything!

**Backend Needed:**
```java
// Create new User entity (different from removed one):
@Entity
@Table(name = "app_user")  // Avoid "user" reserved keyword
public class AppUser extends BaseEntity {
    private String username;
    private String password;  // BCrypt encoded
    private String email;
    private String role;  // DOCTOR, SELLER, CUSTOMER, ADMIN
    private boolean enabled;
}

// Create AuthController for login/register
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @PostMapping("/login")   // ❌ Create
    @PostMapping("/register") // ❌ Create
    @PostMapping("/logout")   // ❌ Create
}

// Update SecurityConfig to require authentication
http.authorizeHttpRequests(authz -> authz
    .requestMatchers("/api/auth/**").permitAll()
    .requestMatchers("/api/recipes/**").hasAnyRole("DOCTOR", "ADMIN")
    .requestMatchers("/api/medicines/**").hasAnyRole("SELLER", "ADMIN")
    .anyRequest().authenticated()
)
```

**Frontend Needed:**
```html
<!-- templates/login.html -->
<!-- templates/register.html -->
```

---

## 📊 Work Breakdown

### Week 1: Foundation
- [ ] Day 1-2: Create Customer entity, repository, service, API
- [ ] Day 3: Fix Recipe relationships (add Customer, Medicines)
- [ ] Day 4-5: Create Customer UI (list, create, edit pages)

### Week 2: Core Features
- [ ] Day 1-2: Create Doctor UI (list, create, edit pages)
- [ ] Day 3-5: Create Recipe UI (list, create, edit pages)

### Week 3: Security & Polish
- [ ] Day 1-3: Implement authentication system
- [ ] Day 4: Add login/register UI
- [ ] Day 5: Role-based access control

### Week 4: Testing & Improvements
- [ ] Day 1-2: Add validation and error handling
- [ ] Day 3: Add success/error messages to UI
- [ ] Day 4: Testing
- [ ] Day 5: Documentation

---

## 🎯 Simplest Path Forward (This Week)

### Start Here (4-6 hours):
1. **Create Customer Entity** (30 min)
2. **Create CustomerRepository** (10 min)
3. **Create CustomerService** (30 min)
4. **Create CustomerApiController** (30 min)
5. **Add sample customers to data.sql** (15 min)
6. **Update Recipe to reference Customer** (30 min)
7. **Create CustomerViewController** (30 min)
8. **Create customers.html** (1 hour)
9. **Create create-customer.html** (45 min)
10. **Create edit-customer.html** (45 min)
11. **Add "Customers" link to navigation** (5 min)

After this, you'll have:
- ✅ Complete Customer management
- ✅ Prescriptions linked to customers
- ✅ 3 out of 4 main entities working

---

## 💡 Code Snippets to Get Started

### Customer Entity Template:
```java
package com.inf.cscb869_pharmacy.customer.entity;

import com.inf.cscb869_pharmacy.common.BaseEntity;
import com.inf.cscb869_pharmacy.recipe.entity.Recipe;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
public class Customer extends BaseEntity {
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100)
    private String name;
    
    @Min(value = 0, message = "Age must be positive")
    @Max(value = 150, message = "Invalid age")
    private Integer age;
    
    @Email(message = "Invalid email format")
    private String email;
    
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phoneNumber;
    
    @Column(length = 1000)
    private String allergies;  // Comma-separated or JSON
    
    @OneToMany(mappedBy = "customer")
    private Set<Recipe> prescriptions;
}
```

### CustomerRepository Template:
```java
package com.inf.cscb869_pharmacy.customer.repository;

import com.inf.cscb869_pharmacy.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByNameContainingIgnoreCase(String name);
    Customer findByEmail(String email);
}
```

### Add to fragments.html navigation:
```html
<li class="nav-item">
    <a class="nav-link" href="/customers">
        👥 Customers
    </a>
</li>
```

---

## 🚨 Common Mistakes to Avoid

1. ❌ Don't create circular dependencies between entities
2. ❌ Don't forget `@JsonIgnore` on bidirectional relationships
3. ❌ Don't hardcode IDs in data.sql (use SELECT subqueries)
4. ❌ Don't forget to add navigation links after creating pages
5. ❌ Don't skip validation on DTOs and entities
6. ❌ Don't forget to handle null cases in Thymeleaf templates

---

## 📞 Need Help?

**Stuck on Customer entity?** Copy the template above!  
**Not sure about Recipe fixes?** Check IMPLEMENTATION_STATUS.md for details  
**UI not working?** Check that you have both ViewController AND templates  

**Good luck! Start with Customer management - it's the foundation for everything else! 🚀**
