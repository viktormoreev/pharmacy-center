package com.inf.cscb869_pharmacy.customer.controller;

import com.inf.cscb869_pharmacy.config.SecurityConfig;
import com.inf.cscb869_pharmacy.customer.service.CustomerService;
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

@WebMvcTest(controllers = CustomerViewController.class)
@Import(SecurityConfig.class)
class CustomerViewControllerSecurityWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private DoctorService doctorService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void customerCreateShouldAlsoBeAllowedForAdminWithDifferentPayload() throws Exception {
        when(customerService.createCustomer(org.mockito.ArgumentMatchers.any())).thenAnswer(inv -> inv.getArgument(0));
        mockMvc.perform(post("/customers/create")
                        .with(csrf())
                        .param("name", "Alice")
                        .param("email", "alice@pharmacy.com")
                        .param("phone", "+359888111222"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void customerCreateShouldBeAllowedForAdmin() throws Exception {
        when(customerService.createCustomer(org.mockito.ArgumentMatchers.any())).thenAnswer(inv -> inv.getArgument(0));
        mockMvc.perform(post("/customers/create")
                        .with(csrf())
                        .param("name", "Bob")
                        .param("email", "bob@pharmacy.com")
                        .param("phone", "+359888111223"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void customerCreateShouldBeForbiddenForDoctorRole() throws Exception {
        mockMvc.perform(get("/customers/create"))
                .andExpect(status().isForbidden());
    }
}
