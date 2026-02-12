package com.inf.cscb869_pharmacy.report.controller;

import com.inf.cscb869_pharmacy.config.SecurityConfig;
import com.inf.cscb869_pharmacy.customer.service.CustomerService;
import com.inf.cscb869_pharmacy.doctor.service.DoctorService;
import com.inf.cscb869_pharmacy.report.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReportViewController.class)
@Import(SecurityConfig.class)
class ReportViewControllerSecurityWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @MockBean
    private DoctorService doctorService;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @Test
    @WithMockUser(roles = "DOCTOR")
    void patientsByPrimaryDoctorShouldBeForbiddenForDoctor() throws Exception {
        // This report is restricted to admin roles.
        mockMvc.perform(get("/reports/patients-by-primary-doctor"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void apiReportsPathShouldPassSecurityForAdmin() throws Exception {
        // No handler exists for this path; 404 means security allowed request through.
        mockMvc.perform(get("/api/reports/non-existing"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void reportsIndexShouldBeForbiddenForCustomer() throws Exception {
        // Customers cannot access reports UI.
        mockMvc.perform(get("/reports"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void apiReportsPathShouldBeForbiddenForCustomer() throws Exception {
        // Customers cannot access reports API either.
        mockMvc.perform(get("/api/reports/non-existing"))
                .andExpect(status().isForbidden());
    }
}
