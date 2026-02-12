package com.inf.cscb869_pharmacy.customer.service;

import com.inf.cscb869_pharmacy.customer.entity.Customer;
import com.inf.cscb869_pharmacy.customer.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Test
    void createCustomerShouldSetActiveTrueByDefault() {
        Customer customer = customer("Alice", "alice@pharmacy.com");
        customer.setActive(null);

        when(customerRepository.existsByEmail("alice@pharmacy.com")).thenReturn(false);
        when(customerRepository.save(customer)).thenReturn(customer);
        Customer result = customerService.createCustomer(customer);
        assertThat(result).isSameAs(customer);
        assertThat(customer.getActive()).isTrue();
        verify(customerRepository).save(customer);
    }

    @Test
    void createCustomerShouldThrowWhenEmailExists() {
        Customer customer = customer("Alice", "alice@pharmacy.com");
        when(customerRepository.existsByEmail("alice@pharmacy.com")).thenReturn(true);
        assertThatThrownBy(() -> customerService.createCustomer(customer))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already exists: alice@pharmacy.com");
    }

    @Test
    void updateCustomerShouldUpdateFieldsAndPersist() {
        Customer existing = customer("Old Name", "old@pharmacy.com");
        existing.setId(7L);
        existing.setActive(true);

        Customer update = customer("New Name", "new@pharmacy.com");
        update.setAge(45);
        update.setPhone("+359888111222");
        update.setAddress("Sofia");
        update.setAllergies("Penicillin");
        update.setMedicalHistory("Hypertension");
        update.setInsuranceNumber("INS-001");
        update.setActive(false);

        when(customerRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(customerRepository.existsByEmail("new@pharmacy.com")).thenReturn(false);
        when(customerRepository.save(existing)).thenReturn(existing);
        Customer result = customerService.updateCustomer(7L, update);
        assertThat(result).isSameAs(existing);
        assertThat(existing.getName()).isEqualTo("New Name");
        assertThat(existing.getEmail()).isEqualTo("new@pharmacy.com");
        assertThat(existing.getAge()).isEqualTo(45);
        assertThat(existing.getPhone()).isEqualTo("+359888111222");
        assertThat(existing.getAddress()).isEqualTo("Sofia");
        assertThat(existing.getAllergies()).isEqualTo("Penicillin");
        assertThat(existing.getMedicalHistory()).isEqualTo("Hypertension");
        assertThat(existing.getInsuranceNumber()).isEqualTo("INS-001");
        assertThat(existing.getActive()).isFalse();
        verify(customerRepository).save(existing);
    }

    @Test
    void updateCustomerShouldThrowWhenChangingToExistingEmail() {
        Customer existing = customer("Old Name", "old@pharmacy.com");
        existing.setId(8L);

        Customer update = customer("New Name", "taken@pharmacy.com");

        when(customerRepository.findById(8L)).thenReturn(Optional.of(existing));
        when(customerRepository.existsByEmail("taken@pharmacy.com")).thenReturn(true);
        assertThatThrownBy(() -> customerService.updateCustomer(8L, update))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already exists: taken@pharmacy.com");
    }

    @Test
    void deleteCustomerShouldSoftDeleteBySettingInactive() {
        Customer existing = customer("Delete Me", "delete@pharmacy.com");
        existing.setId(9L);
        existing.setActive(true);

        when(customerRepository.findById(9L)).thenReturn(Optional.of(existing));
        when(customerRepository.save(existing)).thenReturn(existing);
        customerService.deleteCustomer(9L);
        assertThat(existing.getActive()).isFalse();
        verify(customerRepository).save(existing);
    }

    @Test
    void getCustomersByPrimaryDoctorIdShouldReturnOnlyActiveCustomers() {
        Customer active = customer("Active", "active@pharmacy.com");
        active.setActive(true);
        Customer inactive = customer("Inactive", "inactive@pharmacy.com");
        inactive.setActive(false);

        when(customerRepository.findByPrimaryDoctorId(10L)).thenReturn(java.util.List.of(active, inactive));

        java.util.List<Customer> result = customerService.getCustomersByPrimaryDoctorId(10L);

        assertThat(result).containsExactly(active);
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
