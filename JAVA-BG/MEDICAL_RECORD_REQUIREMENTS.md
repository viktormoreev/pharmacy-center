# 📋 Задание: Медицинско Досие на Пациента
**Дата:** 26 Януари 2026  
**Проект:** Система за Медицински Картон и Проследяване на Пациенти

---

## ✅ Статус към 10 Февруари 2026 (MVP)

### Готово

- Основни домейни: Пациенти, Лекари, Прегледи/Рецепти, Диагнози, Болнични
- История на заболяванията по пациент (вкл. customer self-history UI)
- Ролево управление на достъп (ADMIN/DOCTOR/PHARMACIST/CUSTOMER)
- Справки за диагнози, посещения и болнични (вкл. месечна статистика)
- PostgreSQL-съвместим отчет за болнични по месеци (`EXTRACT` вместо `YEAR/MONTH`)

### Остава

- Финална валидация, че Keycloak акаунтите съвпадат 1:1 с вътрешните записи по лекар/пациент
- Пълни автоматизирани тестове за RBAC + критични бизнес сценарии
- Миграции (Flyway/Liquibase) за стабилни schema промени

---

## 🎯 АНАЛИЗ НА ИЗИСКВАНИЯТА

### Какво Имаме (✅ Налично)

| Изискване | Статус | Компонент |
|-----------|--------|-----------|
| Пациенти с име | ✅ ГОТОВО | Customer.name |
| Лекари с име | ✅ ГОТОВО | Doctor.name |
| Посещения при лекар | ✅ ГОТОВО | Recipe (преглед) |
| Диагноза | ✅ ГОТОВО | Recipe.diagnosis |
| Лечение (лекарства) | ✅ ГОТОВО | RecipeMedicine |
| История на заболявания | ✅ ГОТОВО | Customer.recipes |
| Връзка лекар-пациент | ✅ ГОТОВО | Recipe.doctor + Recipe.customer |

### Какво Липсва (❌ Необходимо)

| Изискване | Статус | Приоритет | Компонент |
|-----------|--------|-----------|-----------|
| Уникален номер на лекар | ✅ **ГОТОВО** | **ВИСОК** | Doctor.licenseNumber |
| Специалност на лекар | ✅ **ГОТОВО** | **ВИСОК** | Doctor.specialty |
| Регистрация като личен лекар | ✅ **ГОТОВО** | **ВИСОК** | Doctor.isPrimaryDoctor |
| ЕГН на пациент | ✅ **ГОТОВО** | **ВИСОК** | Customer.egn |
| Здравна осигуровка (6 месеца) | ✅ **ГОТОВО** | **ВИСОК** | Customer.insurancePaidUntil |
| Личен лекар на пациент | ✅ **ГОТОВО** | **КРИТИЧЕН** | Customer.primaryDoctor |
| Болничен лист | ✅ **ГОТОВО** | **ВИСОК** | Recipe.sickLeave |
| Брой дни болничен | ✅ **ГОТОВО** | **ВИСОК** | Recipe.sickLeaveDays |
| Дата на болничен | ✅ **ГОТОВО** | **ВИСОК** | Recipe.sickLeaveStartDate |
| **Справки/Отчети:** | | | |
| Пациенти с дадена диагноза | ✅ **ГОТОВО** | **СРЕДЕН** | ReportService + UI |
| Пациенти при личен лекар | ✅ **ГОТОВО** | **СРЕДЕН** | ReportService + UI |
| Посещения при всеки лекар | ✅ **ГОТОВО** | **СРЕДЕН** | ReportService + UI |
| История за пациент (UI) | ✅ **ГОТОВО** | **СРЕДЕН** | CustomerController + UI |

---

## 📊 ПЛАН ЗА ИМПЛЕМЕНТАЦИЯ

### **Фаза 1: Разширяване на Entity класовете** (2-3 часа)

#### 1.1 Разширяване на Doctor Entity
```java
@Entity
@Getter
@Setter
public class Doctor extends BaseEntity {
    
    private String name;
    
    // ✅ НОВО: Уникален идентификационен номер
    @Column(unique = true, nullable = false, length = 20)
    @NotBlank(message = "License number is required")
    private String licenseNumber; // УИН на лекаря
    
    // ✅ НОВО: Специалност
    @Column(nullable = false, length = 100)
    @NotBlank(message = "Specialty is required")
    private String specialty; // Кардиолог, Педиатър, и т.н.
    
    // ✅ НОВО: Може ли да бъде личен лекар
    @Column(nullable = false)
    @Builder.Default
    private Boolean isPrimaryDoctor = true; // Дали може да бъде личен лекар
    
    // Допълнителни полета за контакт (опционално)
    @Column(length = 100)
    private String email;
    
    @Column(length = 20)
    private String phoneNumber;
    
    // Съществуващи връзки
    @OneToMany(mappedBy = "doctor")
    @JsonIgnore
    private Set<Recipe> recipes; // Всички прегледи
    
    // ✅ НОВО: Пациенти при този личен лекар
    @OneToMany(mappedBy = "primaryDoctor")
    @JsonIgnore
    private Set<Customer> primaryPatients;
}
```

#### 1.2 Разширяване на Customer Entity
```java
@Entity
@Table(name = "customers")
@Getter
@Setter
public class Customer extends BaseEntity {
    
    // Съществуващи полета
    private String name;
    private Integer age;
    private String email;
    private String phone;
    private String address;
    private LocalDate dateOfBirth;
    private String allergies;
    private String medicalHistory;
    
    // ✅ НОВО: ЕГН (Единен Граждански Номер)
    @Column(unique = true, nullable = false, length = 10)
    @Pattern(regexp = "^[0-9]{10}$", message = "EGN must be 10 digits")
    @NotBlank(message = "EGN is required")
    private String egn;
    
    // ✅ НОВО: Здравна осигуровка платена до (дата)
    @Column(name = "insurance_paid_until")
    private LocalDate insurancePaidUntil;
    
    // ✅ НОВО: Личен лекар (Many-to-One)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_doctor_id", nullable = false)
    @NotNull(message = "Primary doctor is required")
    private Doctor primaryDoctor;
    
    // Съществуващи връзки
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Recipe> recipes; // История на заболяванията
    
    private String insuranceNumber;
    private Boolean active;
    
    // ✅ НОВО: Метод за проверка на здравна осигуровка
    public boolean hasValidInsurance() {
        if (insurancePaidUntil == null) return false;
        return !insurancePaidUntil.isBefore(LocalDate.now().minusMonths(6));
    }
}
```

#### 1.3 Разширяване на Recipe Entity (Преглед)
```java
@Entity
@Getter
@Setter
public class Recipe extends BaseEntity {
    
    // Съществуващи полета
    private LocalDate creationDate; // Дата на посещение
    
    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor; // Лекар, който е прегледал
    
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer; // Пациент
    
    private RecipeStatus status;
    private String diagnosis; // Поставена диагноза
    private String notes; // Забележки
    private LocalDate expirationDate;
    
    @OneToMany(mappedBy = "recipe")
    private List<RecipeMedicine> recipeMedicines; // Назначено лечение
    
    // ✅ НОВО: Болничен лист
    @Column(name = "sick_leave")
    @Builder.Default
    private Boolean sickLeave = false; // Издаден ли е болничен
    
    // ✅ НОВО: Брой дни болничен
    @Column(name = "sick_leave_days")
    private Integer sickLeaveDays; // За колко дни е болничния
    
    // ✅ НОВО: Начална дата на болничен
    @Column(name = "sick_leave_start_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate sickLeaveStartDate; // От коя дата започва болничния
    
    // Изчислена крайна дата на болничен
    public LocalDate getSickLeaveEndDate() {
        if (sickLeaveStartDate != null && sickLeaveDays != null) {
            return sickLeaveStartDate.plusDays(sickLeaveDays);
        }
        return null;
    }
}
```

---

### **Фаза 2: Database Migration** (1 час)

#### 2.1 SQL Migration Script
Създаване на файл: `src/main/resources/db/migration/V2__add_medical_record_fields.sql`

```sql
-- Добавяне на нови полета към Doctor
ALTER TABLE doctor 
ADD COLUMN license_number VARCHAR(20) UNIQUE,
ADD COLUMN specialty VARCHAR(100),
ADD COLUMN is_primary_doctor BOOLEAN DEFAULT TRUE,
ADD COLUMN email VARCHAR(100),
ADD COLUMN phone_number VARCHAR(20);

-- Добавяне на нови полета към Customer
ALTER TABLE customers 
ADD COLUMN egn VARCHAR(10) UNIQUE,
ADD COLUMN insurance_paid_until DATE,
ADD COLUMN primary_doctor_id BIGINT;

-- Foreign key constraint
ALTER TABLE customers 
ADD CONSTRAINT fk_customer_primary_doctor 
FOREIGN KEY (primary_doctor_id) REFERENCES doctor(id);

-- Добавяне на нови полета към Recipe
ALTER TABLE recipe 
ADD COLUMN sick_leave BOOLEAN DEFAULT FALSE,
ADD COLUMN sick_leave_days INTEGER,
ADD COLUMN sick_leave_start_date DATE;

-- Update existing doctors with dummy data (for testing)
UPDATE doctor SET 
    license_number = CONCAT('UIN-', id),
    specialty = 'General Practitioner',
    is_primary_doctor = TRUE
WHERE license_number IS NULL;

-- Update existing customers with dummy EGN (for testing)
UPDATE customers SET 
    egn = CONCAT('9999999', LPAD(CAST(id AS VARCHAR), 3, '0')),
    insurance_paid_until = CURRENT_DATE + INTERVAL '6 months'
WHERE egn IS NULL;
```

#### 2.2 Актуализиране на data.sql
```sql
-- Обновяване на примерни лекари
UPDATE doctor SET 
    license_number = 'UIN-12345', 
    specialty = 'General Practitioner',
    is_primary_doctor = TRUE,
    email = 'dr.wilson@hospital.bg',
    phone_number = '+359888123456'
WHERE name = 'Dr. James Wilson';

-- Обновяване на примерни пациенти
UPDATE customers SET 
    egn = '8901234567',
    insurance_paid_until = '2026-07-26',
    primary_doctor_id = (SELECT id FROM doctor WHERE name = 'Dr. James Wilson' LIMIT 1)
WHERE name = 'John Smith';
```

---

### **Фаза 3: Repository Layer** (2 часа)

#### 3.1 Разширяване на DoctorRepository
```java
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    
    // Намиране по УИН
    Optional<Doctor> findByLicenseNumber(String licenseNumber);
    
    // Само лекари, които могат да бъдат лични лекари
    List<Doctor> findByIsPrimaryDoctorTrue();
    
    // Лекари по специалност
    List<Doctor> findBySpecialty(String specialty);
    
    // Брой пациенти при всеки личен лекар
    @Query("SELECT d.name, COUNT(c) FROM Doctor d LEFT JOIN Customer c ON c.primaryDoctor = d GROUP BY d.id, d.name")
    List<Object[]> countPatientsByPrimaryDoctor();
    
    // Брой посещения при всеки лекар
    @Query("SELECT d.name, COUNT(r) FROM Doctor d LEFT JOIN Recipe r ON r.doctor = d GROUP BY d.id, d.name")
    List<Object[]> countVisitsByDoctor();
}
```

#### 3.2 Разширяване на CustomerRepository
```java
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    // Намиране по ЕГН
    Optional<Customer> findByEgn(String egn);
    
    // Пациенти с валидна здравна осигуровка
    @Query("SELECT c FROM Customer c WHERE c.insurancePaidUntil >= :sixMonthsAgo")
    List<Customer> findWithValidInsurance(@Param("sixMonthsAgo") LocalDate sixMonthsAgo);
    
    // Пациенти без валидна осигуровка
    @Query("SELECT c FROM Customer c WHERE c.insurancePaidUntil < :sixMonthsAgo OR c.insurancePaidUntil IS NULL")
    List<Customer> findWithoutValidInsurance(@Param("sixMonthsAgo") LocalDate sixMonthsAgo);
    
    // Всички пациенти при даден личен лекар
    List<Customer> findByPrimaryDoctor(Doctor doctor);
    List<Customer> findByPrimaryDoctorId(Long doctorId);
    
    // Брой пациенти при даден лекар
    long countByPrimaryDoctorId(Long doctorId);
}
```

#### 3.3 Разширяване на RecipeRepository
```java
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    
    // Всички прегледи с дадена диагноза
    List<Recipe> findByDiagnosisContainingIgnoreCase(String diagnosis);
    
    // Брой пациенти с дадена диагноза (уникални)
    @Query("SELECT COUNT(DISTINCT r.customer) FROM Recipe r WHERE LOWER(r.diagnosis) LIKE LOWER(CONCAT('%', :diagnosis, '%'))")
    long countDistinctPatientsByDiagnosis(@Param("diagnosis") String diagnosis);
    
    // Всички прегледи при даден лекар
    List<Recipe> findByDoctorId(Long doctorId);
    
    // Брой посещения при даден лекар
    long countByDoctorId(Long doctorId);
    
    // История на пациент (всички прегледи)
    List<Recipe> findByCustomerIdOrderByCreationDateDesc(Long customerId);
    
    // Прегледи с издаден болничен
    List<Recipe> findBySickLeaveTrue();
    
    // Болнични за даден пациент
    List<Recipe> findByCustomerIdAndSickLeaveTrueOrderByCreationDateDesc(Long customerId);
    
    // Най-много издадени болнични (по лекар)
    @Query("SELECT r.doctor.name, COUNT(r) FROM Recipe r WHERE r.sickLeave = TRUE GROUP BY r.doctor.id, r.doctor.name ORDER BY COUNT(r) DESC")
    List<Object[]> countSickLeavesByDoctor();
    
    // Най-често срещани диагнози
    @Query("SELECT r.diagnosis, COUNT(r) as cnt FROM Recipe r WHERE r.diagnosis IS NOT NULL GROUP BY r.diagnosis ORDER BY cnt DESC")
    List<Object[]> findMostCommonDiagnoses();
}
```

---

### **Фаза 4: Service Layer** (3 часа)

#### 4.1 Разширяване на DoctorService
```java
public interface DoctorService {
    // Съществуващи методи
    List<Doctor> getDoctors();
    Doctor getDoctor(long id);
    Doctor createDoctor(Doctor doctor);
    Doctor updateDoctor(Doctor doctor, long id);
    void deleteDoctor(long id);
    long countDoctors();
    
    // ✅ НОВИ методи
    Doctor getDoctorByLicenseNumber(String licenseNumber);
    List<Doctor> getPrimaryDoctors(); // Само лични лекари
    List<Doctor> getDoctorsBySpecialty(String specialty);
    Map<String, Long> getPatientCountByPrimaryDoctor();
    Map<String, Long> getVisitCountByDoctor();
}
```

#### 4.2 Разширяване на CustomerService
```java
public interface CustomerService {
    // Съществуващи методи
    List<Customer> getCustomers();
    Customer getCustomer(long id);
    Customer createCustomer(Customer customer);
    Customer updateCustomer(Customer customer, long id);
    void deleteCustomer(long id);
    
    // ✅ НОВИ методи
    Customer getCustomerByEgn(String egn);
    List<Customer> getCustomersWithValidInsurance();
    List<Customer> getCustomersWithoutValidInsurance();
    List<Customer> getPatientsByPrimaryDoctor(Long doctorId);
    long countPatientsByPrimaryDoctor(Long doctorId);
    boolean hasValidInsurance(Long customerId);
}
```

#### 4.3 Нов ReportService (за справки)
```java
public interface ReportService {
    
    // a) Списък с пациенти с дадена диагноза
    List<Customer> getPatientsByDiagnosis(String diagnosis);
    long countPatientsByDiagnosis(String diagnosis);
    
    // b) Най-често срещани диагнози
    List<DiagnosisReportDTO> getMostCommonDiagnoses();
    
    // c) Пациенти при даден личен лекар
    List<Customer> getPatientsByPrimaryDoctor(Long doctorId);
    
    // d) Брой пациенти при всеки личен лекар
    Map<String, Long> getPatientCountPerPrimaryDoctor();
    
    // e) Брой посещения при всеки лекар
    Map<String, Long> getVisitCountPerDoctor();
    
    // f) История на пациент
    List<Recipe> getPatientMedicalHistory(Long customerId);
    
    // g) Всички прегледи в период
    List<Recipe> getExaminationsInDateRange(LocalDate startDate, LocalDate endDate);
    
    // h) Прегледи при даден лекар в период
    List<Recipe> getDoctorExaminationsInDateRange(Long doctorId, LocalDate startDate, LocalDate endDate);
    
    // i) Месец с най-много издадени болнични
    Map<String, Long> getSickLeavesByMonth();
    
    // j) Лекари с най-много издадени болнични
    Map<String, Long> getDoctorsSickLeaveRanking();
}
```

---

### **Фаза 5: Controller Layer (Backend)** (2 часа)

#### 5.1 Нов ReportController
```java
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportApiController {
    
    private final ReportService reportService;
    
    @GetMapping("/patients-by-diagnosis")
    public ResponseEntity<List<Customer>> getPatientsByDiagnosis(@RequestParam String diagnosis) {
        return ResponseEntity.ok(reportService.getPatientsByDiagnosis(diagnosis));
    }
    
    @GetMapping("/common-diagnoses")
    public ResponseEntity<List<DiagnosisReportDTO>> getCommonDiagnoses() {
        return ResponseEntity.ok(reportService.getMostCommonDiagnoses());
    }
    
    @GetMapping("/patients-by-primary-doctor/{doctorId}")
    public ResponseEntity<List<Customer>> getPatientsByPrimaryDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(reportService.getPatientsByPrimaryDoctor(doctorId));
    }
    
    @GetMapping("/patient-count-by-primary-doctor")
    public ResponseEntity<Map<String, Long>> getPatientCountByPrimaryDoctor() {
        return ResponseEntity.ok(reportService.getPatientCountPerPrimaryDoctor());
    }
    
    @GetMapping("/visit-count-by-doctor")
    public ResponseEntity<Map<String, Long>> getVisitCountByDoctor() {
        return ResponseEntity.ok(reportService.getVisitCountPerDoctor());
    }
    
    @GetMapping("/patient-history/{customerId}")
    public ResponseEntity<List<Recipe>> getPatientHistory(@PathVariable Long customerId) {
        return ResponseEntity.ok(reportService.getPatientMedicalHistory(customerId));
    }
    
    @GetMapping("/sick-leaves-by-month")
    public ResponseEntity<Map<String, Long>> getSickLeavesByMonth() {
        return ResponseEntity.ok(reportService.getSickLeavesByMonth());
    }
    
    @GetMapping("/doctors-sick-leave-ranking")
    public ResponseEntity<Map<String, Long>> getDoctorsSickLeaveRanking() {
        return ResponseEntity.ok(reportService.getDoctorsSickLeaveRanking());
    }
}
```

---

### **Фаза 6: Frontend (UI)** (8-10 часа)

#### 6.1 Актуализиране на Doctor Forms
- ✅ Добавяне на полета: `licenseNumber`, `specialty`, `isPrimaryDoctor`
- ✅ Валидация на УИН (уникален)
- ✅ Dropdown за специалност

#### 6.2 Актуализиране на Customer Forms
- ✅ Добавяне на поле за ЕГН (10 цифри)
- ✅ Dropdown за избор на Личен лекар
- ✅ Дата на здравна осигуровка
- ✅ Индикатор за валидна/невалидна осигуровка

#### 6.3 Актуализиране на Recipe Forms
- ✅ Checkbox за болничен лист
- ✅ Полета: брой дни, начална дата (conditional rendering)
- ✅ Автоматично изчисление на крайна дата

#### 6.4 Нови Report Pages
```
/templates/reports/
  ├── patients-by-diagnosis.html       (справка a)
  ├── common-diagnoses.html            (справка b)
  ├── patients-by-primary-doctor.html  (справка c)
  ├── primary-doctor-statistics.html   (справка d)
  ├── doctor-visit-statistics.html     (справка e)
  ├── patient-medical-history.html     (справка f)
  ├── examinations-report.html         (справка g, h)
  ├── sick-leaves-by-month.html        (справка i)
  └── doctors-sick-leave-ranking.html  (справка j)
```

#### 6.5 Подобрена Customer Profile Page
- ✅ Секция: Личен лекар (име, специалност, контакт)
- ✅ Секция: Здравна осигуровка (статус, дата до когато е платена)
- ✅ Секция: История на заболяванията (таблица)
  - Дата на преглед
  - Лекар
  - Диагноза
  - Лечение (лекарства)
  - Болничен (ако има)
- ✅ Филтриране на история по дата/лекар/диагноза

---

## 📅 ВРЕМЕВИ ПЛАН

| Фаза | Задачи | Оценка време | Приоритет |
|------|--------|--------------|-----------|
| **Фаза 1** | Разширяване на entities | 2-3 часа | КРИТИЧЕН |
| **Фаза 2** | Database migration | 1 час | КРИТИЧЕН |
| **Фаза 3** | Repository queries | 2 часа | ВИСОК |
| **Фаза 4** | Service methods | 3 часа | ВИСОК |
| **Фаза 5** | REST API controllers | 2 часа | ВИСОК |
| **Фаза 6** | Frontend UI | 8-10 часа | СРЕДЕН |
| **Тестване** | Manual testing | 4 часа | ВИСОК |
| **Документация** | User guide | 2 часа | НИСЪК |

**ОБЩО: 24-27 часа (3-4 работни дни)**

---

## 🎯 СЛЕДВАЩИ СТЪПКИ

### ⚡ Започни с:
1. **Фаза 1: Разширяване на Doctor Entity**
   - Добави: `licenseNumber`, `specialty`, `isPrimaryDoctor`
   - Обнови DoctorViewController формуляри
   - Тествай CRUD операциите

2. **Фаза 1: Разширяване на Customer Entity**
   - Добави: `egn`, `insurancePaidUntil`, `primaryDoctor`
   - Обнови CustomerViewController формуляри
   - Добави dropdown за избор на личен лекар

3. **Фаза 1: Разширяване на Recipe Entity**
   - Добави: `sickLeave`, `sickLeaveDays`, `sickLeaveStartDate`
   - Обнови RecipeViewController формуляри
   - Добави conditional rendering за болничен

Готов ли си да започнем с Фаза 1? 🚀
