package com.inf.cscb869_pharmacy.doctor.controller;

import com.inf.cscb869_pharmacy.config.SecurityConfig;
import com.inf.cscb869_pharmacy.doctor.service.DoctorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DoctorViewController.class)
@Import(SecurityConfig.class)
class DoctorViewControllerSecurityWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DoctorService doctorService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void doctorCreateShouldBeAllowedForAdmin() throws Exception {
        when(doctorService.createDoctor(org.mockito.ArgumentMatchers.any())).thenAnswer(inv -> inv.getArgument(0));
        mockMvc.perform(post("/doctors/create")
                        .with(csrf())
                        .param("name", "Dr. New")
                        .param("licenseNumber", "UIN-NEW")
                        .param("specialty", "General"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "PHARMACIST")
    void doctorCreateShouldBeAllowedForPharmacist() throws Exception {
        when(doctorService.createDoctor(org.mockito.ArgumentMatchers.any())).thenAnswer(inv -> inv.getArgument(0));
        mockMvc.perform(post("/doctors/create")
                        .with(csrf())
                        .param("name", "Dr. New")
                        .param("licenseNumber", "UIN-NEW2")
                        .param("specialty", "General"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void doctorCreateShouldBeForbiddenForDoctorRole() throws Exception {
        mockMvc.perform(get("/doctors/create"))
                .andExpect(status().isForbidden());
    }
}
