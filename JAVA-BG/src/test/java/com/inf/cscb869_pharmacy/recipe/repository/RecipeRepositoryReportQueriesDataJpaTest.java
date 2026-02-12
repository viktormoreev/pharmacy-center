package com.inf.cscb869_pharmacy.recipe.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.test.database.replace=NONE",
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.url=jdbc:h2:mem:reporttests;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;NON_KEYWORDS=DOCTOR"
})
class RecipeRepositoryReportQueriesDataJpaTest {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Seed minimal doctors/patients/examinations for aggregate query tests.
        jdbcTemplate.update(
                "insert into doctor (id, name, license_number, specialty, is_primary_doctor, email) values (?,?,?,?,?,?)",
                101L, "Dr. One", "UIN-100", "General", true, "one@clinic.com"
        );
        jdbcTemplate.update(
                "insert into doctor (id, name, license_number, specialty, is_primary_doctor, email) values (?,?,?,?,?,?)",
                102L, "Dr. Two", "UIN-200", "General", true, "two@clinic.com"
        );

        jdbcTemplate.update(
                "insert into customers (id, name, egn, age, email, active, primary_doctor_id) values (?,?,?,?,?,?,?)",
                201L, "Alice", "1111111111", 30, "alice@mail.com", true, 101L
        );
        jdbcTemplate.update(
                "insert into customers (id, name, egn, age, email, active, primary_doctor_id) values (?,?,?,?,?,?,?)",
                202L, "Bob", "2222222222", 35, "bob@mail.com", true, 102L
        );

        insertRecipe(301L, LocalDate.of(2026, 1, 10), 101L, 201L, "ACTIVE", "Flu", true);
        insertRecipe(302L, LocalDate.of(2026, 1, 20), 101L, 202L, "ACTIVE", "Flu", true);
        insertRecipe(303L, LocalDate.of(2026, 2, 5), 102L, 201L, "ACTIVE", "Covid", true);
        insertRecipe(304L, LocalDate.of(2026, 2, 15), 102L, 202L, "ACTIVE", null, false);
        insertRecipe(305L, LocalDate.of(2026, 2, 20), 102L, 202L, "ACTIVE", "", false);
    }

    @Test
    void countSickLeavesByMonthShouldGroupAndOrderDescending() {
        // Act
        List<Object[]> rows = recipeRepository.countSickLeavesByMonth();

        // Assert
        assertThat(rows).hasSize(2);

        assertThat(asInt(rows.get(0)[0])).isEqualTo(2026);
        assertThat(asInt(rows.get(0)[1])).isEqualTo(2);
        assertThat(asLong(rows.get(0)[2])).isEqualTo(1L);

        assertThat(asInt(rows.get(1)[0])).isEqualTo(2026);
        assertThat(asInt(rows.get(1)[1])).isEqualTo(1);
        assertThat(asLong(rows.get(1)[2])).isEqualTo(2L);
    }

    @Test
    void findMostCommonDiagnosesShouldExcludeNullAndBlankAndSortByCount() {
        // Act
        List<Object[]> rows = recipeRepository.findMostCommonDiagnoses();

        // Assert
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0)[0]).isEqualTo("Flu");
        assertThat(asLong(rows.get(0)[1])).isEqualTo(2L);
        assertThat(rows.get(1)[0]).isEqualTo("Covid");
        assertThat(asLong(rows.get(1)[1])).isEqualTo(1L);
    }

    @Test
    void countDistinctPatientsByDiagnosisShouldReturnUniqueCustomerCount() {
        // Act
        long fluPatients = recipeRepository.countDistinctPatientsByDiagnosis("flu");
        long covidPatients = recipeRepository.countDistinctPatientsByDiagnosis("COVID");

        // Assert
        assertThat(fluPatients).isEqualTo(2L);
        assertThat(covidPatients).isEqualTo(1L);
    }

    private static int asInt(Object value) {
        return ((Number) value).intValue();
    }

    private static long asLong(Object value) {
        return ((Number) value).longValue();
    }

    private void insertRecipe(Long id,
                              LocalDate creationDate,
                              Long doctorId,
                              Long customerId,
                              String status,
                              String diagnosis,
                              boolean sickLeave) {
        jdbcTemplate.update(
                "insert into recipe (id, creation_date, doctor_id, customer_id, status, diagnosis, sick_leave) values (?,?,?,?,?,?,?)",
                id, creationDate, doctorId, customerId, status, diagnosis, sickLeave
        );
    }
}
