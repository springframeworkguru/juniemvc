package guru.springframework.juniemvc.repositories;

import guru.springframework.juniemvc.entities.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CustomerRepositoryTest {

    @Autowired
    CustomerRepository customerRepository;

    @Test
    void testSaveCustomer() {
        // Given
        Customer customer = Customer.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .phoneNumber("555-123-4567")
                .addressLine1("123 Main St")
                .addressLine2("Apt 4B")
                .city("Springfield")
                .state("IL")
                .postalCode("62701")
                .build();

        // When
        Customer savedCustomer = customerRepository.save(customer);

        // Then
        assertThat(savedCustomer).isNotNull();
        assertThat(savedCustomer.getId()).isNotNull();
    }

    @Test
    void testGetCustomerById() {
        // Given
        Customer customer = Customer.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .phoneNumber("555-123-4567")
                .addressLine1("123 Main St")
                .addressLine2("Apt 4B")
                .city("Springfield")
                .state("IL")
                .postalCode("62701")
                .build();
        Customer savedCustomer = customerRepository.save(customer);

        // When
        Optional<Customer> fetchedCustomerOptional = customerRepository.findById(savedCustomer.getId());

        // Then
        assertThat(fetchedCustomerOptional).isPresent();
        Customer fetchedCustomer = fetchedCustomerOptional.get();
        assertThat(fetchedCustomer.getName()).isEqualTo("John Doe");
        assertThat(fetchedCustomer.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void testUpdateCustomer() {
        // Given
        Customer customer = Customer.builder()
                .name("Original Name")
                .email("original@example.com")
                .phoneNumber("555-123-4567")
                .addressLine1("123 Main St")
                .addressLine2("Apt 4B")
                .city("Springfield")
                .state("IL")
                .postalCode("62701")
                .build();
        Customer savedCustomer = customerRepository.save(customer);

        // When
        savedCustomer.setName("Updated Name");
        savedCustomer.setEmail("updated@example.com");
        Customer updatedCustomer = customerRepository.save(savedCustomer);

        // Then
        assertThat(updatedCustomer.getName()).isEqualTo("Updated Name");
        assertThat(updatedCustomer.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    void testDeleteCustomer() {
        // Given
        Customer customer = Customer.builder()
                .name("Delete Me")
                .email("delete@example.com")
                .phoneNumber("555-123-4567")
                .addressLine1("123 Main St")
                .city("Springfield")
                .state("IL")
                .postalCode("62701")
                .build();
        Customer savedCustomer = customerRepository.save(customer);

        // When
        customerRepository.deleteById(savedCustomer.getId());
        Optional<Customer> deletedCustomer = customerRepository.findById(savedCustomer.getId());

        // Then
        assertThat(deletedCustomer).isEmpty();
    }

    @Test
    void testListCustomers() {
        // Given
        customerRepository.deleteAll(); // Clear any existing data
        Customer customer1 = Customer.builder()
                .name("Customer 1")
                .email("customer1@example.com")
                .phoneNumber("555-111-1111")
                .addressLine1("111 First St")
                .city("Springfield")
                .state("IL")
                .postalCode("62701")
                .build();
        Customer customer2 = Customer.builder()
                .name("Customer 2")
                .email("customer2@example.com")
                .phoneNumber("555-222-2222")
                .addressLine1("222 Second St")
                .city("Shelbyville")
                .state("IL")
                .postalCode("62565")
                .build();
        customerRepository.saveAll(List.of(customer1, customer2));

        // When
        List<Customer> customers = customerRepository.findAll();

        // Then
        assertThat(customers).hasSize(2);
    }
}