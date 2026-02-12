package com.inf.cscb869_pharmacy.customer.controller;

import com.inf.cscb869_pharmacy.customer.dto.CustomerDTO;
import com.inf.cscb869_pharmacy.customer.entity.Customer;
import com.inf.cscb869_pharmacy.customer.service.CustomerService;
import com.inf.cscb869_pharmacy.doctor.entity.Doctor;
import com.inf.cscb869_pharmacy.doctor.service.DoctorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerViewControllerTest {

    @Mock
    private CustomerService customerService;

    @Mock
    private DoctorService doctorService;

    @InjectMocks
    private CustomerViewController customerViewController;

    @Test
    void listCustomersShouldUseActiveCustomersWhenSearchMissing() {
        List<Customer> active = List.of(customer("Alice", "alice@pharmacy.com"));
        when(customerService.getActiveCustomers()).thenReturn(active);
        ExtendedModelMap model = new ExtendedModelMap();
        String view = customerViewController.listCustomers(null, model, null);
        assertThat(view).isEqualTo("customers/customers");
        assertThat(model.getAttribute("customers")).isEqualTo(active);
    }

    @Test
    void listCustomersShouldUseSearchWhenProvided() {
        List<Customer> found = List.of(customer("Bob", "bob@pharmacy.com"));
        when(customerService.searchByName("bo")).thenReturn(found);
        ExtendedModelMap model = new ExtendedModelMap();
        String view = customerViewController.listCustomers("bo", model, null);
        assertThat(view).isEqualTo("customers/customers");
        assertThat(model.getAttribute("customers")).isEqualTo(found);
        assertThat(model.getAttribute("search")).isEqualTo("bo");
    }

    @Test
    void listCustomersShouldShowPrimaryPatientsForDoctor() {
        Doctor doctor = Doctor.builder()
                .name("Dr. John Smith")
                .licenseNumber("UIN-12345")
                .specialty("General")
                .isPrimaryDoctor(true)
                .email("doctor@pharmacy.com")
                .build();
        doctor.setId(11L);
        when(doctorService.findByEmail("doctor@pharmacy.com")).thenReturn(Optional.of(doctor));

        List<Customer> assigned = List.of(customer("Patient One", "one@pharmacy.com"));
        when(customerService.getCustomersByPrimaryDoctorId(11L)).thenReturn(assigned);

        OidcUser oidcUser = org.mockito.Mockito.mock(OidcUser.class);
        when(oidcUser.getEmail()).thenReturn("doctor@pharmacy.com");
        var auth = new UsernamePasswordAuthenticationToken(
                oidcUser,
                "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_DOCTOR"))
        );

        ExtendedModelMap model = new ExtendedModelMap();
        String view = customerViewController.listCustomers(null, model, auth);

        assertThat(view).isEqualTo("customers/customers");
        assertThat(model.getAttribute("isDoctorUser")).isEqualTo(true);
        assertThat(model.getAttribute("currentDoctorName")).isEqualTo("Dr. John Smith");
        assertThat(model.getAttribute("customers")).isEqualTo(assigned);
    }

    @Test
    void listCustomersShouldShowErrorWhenDoctorMappingMissing() {
        when(doctorService.findByEmail("missing@pharmacy.com")).thenReturn(Optional.empty());

        OidcUser oidcUser = org.mockito.Mockito.mock(OidcUser.class);
        when(oidcUser.getEmail()).thenReturn("missing@pharmacy.com");
        var auth = new UsernamePasswordAuthenticationToken(
                oidcUser,
                "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_DOCTOR"))
        );

        ExtendedModelMap model = new ExtendedModelMap();
        String view = customerViewController.listCustomers(null, model, auth);

        assertThat(view).isEqualTo("customers/customers");
        assertThat(model.getAttribute("isDoctorUser")).isEqualTo(true);
        assertThat(model.getAttribute("customers")).isEqualTo(List.of());
        assertThat(model.getAttribute("errorMessage")).isNotNull();
    }

    @Test
    void createCustomerShouldReturnFormOnValidationErrors() {
        CustomerDTO dto = CustomerDTO.builder().build();
        BindingResult result = new BeanPropertyBindingResult(dto, "customerDTO");
        result.rejectValue("name", "required", "Name required");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        String view = customerViewController.createCustomer(dto, result, redirect);
        assertThat(view).isEqualTo("customers/create-customer");
        verifyNoInteractions(customerService);
    }

    @Test
    void createCustomerShouldPersistAndRedirectOnSuccess() {
        CustomerDTO dto = CustomerDTO.builder()
                .name("Alice")
                .age(30)
                .email("alice@pharmacy.com")
                .phone("+359888111222")
                .build();
        BindingResult result = new BeanPropertyBindingResult(dto, "customerDTO");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        when(customerService.createCustomer(org.mockito.ArgumentMatchers.any(Customer.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        String view = customerViewController.createCustomer(dto, result, redirect);
        assertThat(view).isEqualTo("redirect:/customers");
        assertThat(redirect.getFlashAttributes().get("successMessage")).isEqualTo("Customer created successfully!");

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerService).createCustomer(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Alice");
        assertThat(captor.getValue().getActive()).isTrue();
    }

    @Test
    void createCustomerShouldReturnFormAndAddEmailErrorWhenServiceRejects() {
        CustomerDTO dto = CustomerDTO.builder()
                .name("Alice")
                .age(30)
                .email("dup@pharmacy.com")
                .build();
        BindingResult result = new BeanPropertyBindingResult(dto, "customerDTO");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        when(customerService.createCustomer(org.mockito.ArgumentMatchers.any(Customer.class)))
                .thenThrow(new IllegalArgumentException("Email already exists: dup@pharmacy.com"));
        String view = customerViewController.createCustomer(dto, result, redirect);
        assertThat(view).isEqualTo("customers/create-customer");
        assertThat(result.hasFieldErrors("email")).isTrue();
    }
    
    private static Customer customer(String name, String email) {
        return Customer.builder()
                .name(name)
                .egn("1234567890")
                .age(30)
                .email(email)
                .active(true)
                .build();
    }
}
