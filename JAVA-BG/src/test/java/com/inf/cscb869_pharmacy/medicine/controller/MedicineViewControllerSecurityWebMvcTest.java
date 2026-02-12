package com.inf.cscb869_pharmacy.medicine.controller;

import com.inf.cscb869_pharmacy.config.SecurityConfig;
import com.inf.cscb869_pharmacy.medicine.dto.CreateMedicineDTO;
import com.inf.cscb869_pharmacy.medicine.service.MedicineService;
import com.inf.cscb869_pharmacy.util.MapperUtil;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MedicineViewController.class)
@Import(SecurityConfig.class)
class MedicineViewControllerSecurityWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MedicineService medicineService;

    @MockBean
    private MapperUtil mapperUtil;

    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @Test
    @WithMockUser(roles = "PHARMACIST")
    void showCreateMedicineFormShouldBeAllowedForPharmacist() throws Exception {
        when(mapperUtil.getModelMapper()).thenReturn(new ModelMapper());

        mockMvc.perform(get("/medicines/create-medicine"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void showCreateMedicineFormShouldBeForbiddenForDoctor() throws Exception {
        mockMvc.perform(get("/medicines/create-medicine"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PHARMACIST")
    void createMedicineShouldBeAllowedForPharmacist() throws Exception {
        when(medicineService.createMedicine(any(CreateMedicineDTO.class)))
                .thenReturn(new CreateMedicineDTO("Paracetamol", 12, false));

        mockMvc.perform(post("/medicines/create")
                        .with(csrf())
                        .param("name", "Paracetamol")
                        .param("ageAppropriateness", "12")
                        .param("needsRecipe", "false"))
                .andExpect(status().is3xxRedirection());
    }
}
