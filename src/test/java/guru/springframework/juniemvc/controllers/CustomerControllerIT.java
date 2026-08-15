package guru.springframework.juniemvc.controllers;

import guru.springframework.juniemvc.entities.Customer;
import guru.springframework.juniemvc.models.CustomerDto;
import guru.springframework.juniemvc.repositories.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CustomerControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CustomerRepository customerRepository;

    Customer testCustomer;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();

        // Create test customer
        testCustomer = Customer.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .phoneNumber("555-123-4567")
                .addressLine1("123 Main St")
                .addressLine2("Apt 4B")
                .city("Springfield")
                .state("IL")
                .postalCode("62701")
                .build();

        testCustomer = customerRepository.save(testCustomer);
    }

    @Test
    void testGetAllCustomers() throws Exception {
        mockMvc.perform(get("/api/v1/customers")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(testCustomer.getId())))
                .andExpect(jsonPath("$[0].name", is("John Doe")));
    }

    @Test
    void testGetCustomerById() throws Exception {
        mockMvc.perform(get("/api/v1/customers/" + testCustomer.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testCustomer.getId())))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")));
    }

    @Test
    void testGetCustomerByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/customers/999")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateCustomer() throws Exception {
        CustomerDto customerToCreate = CustomerDto.builder()
                .name("Jane Smith")
                .email("jane.smith@example.com")
                .phoneNumber("555-987-6543")
                .addressLine1("456 Oak Ave")
                .city("Shelbyville")
                .state("IL")
                .postalCode("62565")
                .build();

        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerToCreate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Jane Smith")))
                .andExpect(jsonPath("$.email", is("jane.smith@example.com")));
    }

    @Test
    void testUpdateCustomer() throws Exception {
        CustomerDto customerToUpdate = CustomerDto.builder()
                .version(testCustomer.getVersion())
                .name("John Doe Updated")
                .email("john.updated@example.com")
                .phoneNumber("555-123-4567")
                .addressLine1("123 Main St")
                .addressLine2("Suite 101")
                .city("Springfield")
                .state("IL")
                .postalCode("62701")
                .build();

        mockMvc.perform(put("/api/v1/customers/" + testCustomer.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerToUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testCustomer.getId())))
                .andExpect(jsonPath("$.name", is("John Doe Updated")))
                .andExpect(jsonPath("$.email", is("john.updated@example.com")));
    }

    @Test
    void testUpdateCustomerNotFound() throws Exception {
        CustomerDto customerToUpdate = CustomerDto.builder()
                .name("John Doe Updated")
                .email("john.updated@example.com")
                .phoneNumber("555-123-4567")
                .addressLine1("123 Main St")
                .addressLine2("Suite 101")
                .city("Springfield")
                .state("IL")
                .postalCode("62701")
                .build();

        mockMvc.perform(put("/api/v1/customers/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerToUpdate)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteCustomer() throws Exception {
        mockMvc.perform(delete("/api/v1/customers/" + testCustomer.getId()))
                .andExpect(status().isNoContent());

        // Verify the customer was deleted
        mockMvc.perform(get("/api/v1/customers/" + testCustomer.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteCustomerNotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/customers/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testValidationErrors() throws Exception {
        CustomerDto invalidCustomer = CustomerDto.builder()
                .email("invalid-email") // Invalid email format
                .phoneNumber("555-123-4567")
                // Missing required fields: name, addressLine1, city, state, postalCode
                .build();

        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCustomer)))
                .andExpect(status().isBadRequest());
    }
}