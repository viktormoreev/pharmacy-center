package com.inf.cscb869_pharmacy.customer.controller;

import com.inf.cscb869_pharmacy.customer.dto.CustomerDTO;
import com.inf.cscb869_pharmacy.customer.entity.Customer;
import com.inf.cscb869_pharmacy.customer.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerViewControllerTest {

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerViewController customerViewController;

    @Test
    void listCustomersShouldUseActiveCustomersWhenSearchMissing() {
        // Arrange: no search term should use active customers query.
        List<Customer> active = List.of(customer("Alice", "alice@pharmacy.com"));
        when(customerService.getActiveCustomers()).thenReturn(active);

        // Act: call controller method directly.
        ExtendedModelMap model = new ExtendedModelMap();
        String view = customerViewController.listCustomers(null, model);

        // Assert: expected template and model payload.
        assertThat(view).isEqualTo("customers/customers");
        assertThat(model.getAttribute("customers")).isEqualTo(active);
    }

    @Test
    void listCustomersShouldUseSearchWhenProvided() {
        // Arrange: search term should route through search service method.
        List<Customer> found = List.of(customer("Bob", "bob@pharmacy.com"));
        when(customerService.searchByName("bo")).thenReturn(found);

        // Act
        ExtendedModelMap model = new ExtendedModelMap();
        String view = customerViewController.listCustomers("bo", model);

        // Assert
        assertThat(view).isEqualTo("customers/customers");
        assertThat(model.getAttribute("customers")).isEqualTo(found);
        assertThat(model.getAttribute("search")).isEqualTo("bo");
    }

    @Test
    void createCustomerShouldReturnFormOnValidationErrors() {
        // Arrange: binding result contains validation error.
        CustomerDTO dto = CustomerDTO.builder().build();
        BindingResult result = new BeanPropertyBindingResult(dto, "customerDTO");
        result.rejectValue("name", "required", "Name required");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        // Act
        String view = customerViewController.createCustomer(dto, result, redirect);

        // Assert: stay on form and do not call service.
        assertThat(view).isEqualTo("customers/create-customer");
        verifyNoInteractions(customerService);
    }

    @Test
    void createCustomerShouldPersistAndRedirectOnSuccess() {
        // Arrange: valid DTO and successful service save.
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

        // Act
        String view = customerViewController.createCustomer(dto, result, redirect);

        // Assert: redirect + flash message + mapped entity fields.
        assertThat(view).isEqualTo("redirect:/customers");
        assertThat(redirect.getFlashAttributes().get("successMessage")).isEqualTo("Customer created successfully!");

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerService).createCustomer(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Alice");
        assertThat(captor.getValue().getActive()).isTrue();
    }

    @Test
    void createCustomerShouldReturnFormAndAddEmailErrorWhenServiceRejects() {
        // Arrange: service rejects duplicate email.
        CustomerDTO dto = CustomerDTO.builder()
                .name("Alice")
                .age(30)
                .email("dup@pharmacy.com")
                .build();
        BindingResult result = new BeanPropertyBindingResult(dto, "customerDTO");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        when(customerService.createCustomer(org.mockito.ArgumentMatchers.any(Customer.class)))
                .thenThrow(new IllegalArgumentException("Email already exists: dup@pharmacy.com"));

        // Act
        String view = customerViewController.createCustomer(dto, result, redirect);

        // Assert: controller maps service error back to form field error.
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
