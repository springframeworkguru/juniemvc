package guru.springframework.juniemvc.controllers;

import guru.springframework.juniemvc.models.CustomerDto;
import guru.springframework.juniemvc.services.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    CustomerService customerService;

    CustomerDto testCustomer;

    @BeforeEach
    void setUp() {
        // Create test customer
        testCustomer = CustomerDto.builder()
                .id(1)
                .name("John Doe")
                .email("john.doe@example.com")
                .phoneNumber("555-123-4567")
                .addressLine1("123 Main St")
                .addressLine2("Apt 4B")
                .city("Springfield")
                .state("IL")
                .postalCode("62701")
                .build();
    }

    @Test
    void testGetAllCustomers() throws Exception {
        // Given
        given(customerService.getAllCustomers()).willReturn(Arrays.asList(testCustomer));

        // When/Then
        mockMvc.perform(get("/api/v1/customers")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("John Doe")));
    }

    @Test
    void testGetCustomerById() throws Exception {
        // Given
        given(customerService.getCustomerById(1)).willReturn(Optional.of(testCustomer));

        // When/Then
        mockMvc.perform(get("/api/v1/customers/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")));
    }

    @Test
    void testGetCustomerByIdNotFound() throws Exception {
        // Given
        given(customerService.getCustomerById(1)).willReturn(Optional.empty());

        // When/Then
        mockMvc.perform(get("/api/v1/customers/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateCustomer() throws Exception {
        // Given
        CustomerDto customerToCreate = CustomerDto.builder()
                .name("Jane Smith")
                .email("jane.smith@example.com")
                .phoneNumber("555-987-6543")
                .addressLine1("456 Oak Ave")
                .city("Shelbyville")
                .state("IL")
                .postalCode("62565")
                .build();

        CustomerDto savedCustomer = CustomerDto.builder()
                .id(2)
                .name("Jane Smith")
                .email("jane.smith@example.com")
                .phoneNumber("555-987-6543")
                .addressLine1("456 Oak Ave")
                .city("Shelbyville")
                .state("IL")
                .postalCode("62565")
                .build();

        given(customerService.saveCustomer(any(CustomerDto.class))).willReturn(savedCustomer);

        // When/Then
        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerToCreate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.name", is("Jane Smith")));
    }

    @Test
    void testUpdateCustomer() throws Exception {
        // Given
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

        CustomerDto updatedCustomer = CustomerDto.builder()
                .id(1)
                .name("John Doe Updated")
                .email("john.updated@example.com")
                .phoneNumber("555-123-4567")
                .addressLine1("123 Main St")
                .addressLine2("Suite 101")
                .city("Springfield")
                .state("IL")
                .postalCode("62701")
                .build();

        given(customerService.updateCustomer(anyInt(), any(CustomerDto.class))).willReturn(Optional.of(updatedCustomer));

        // When/Then
        mockMvc.perform(put("/api/v1/customers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerToUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John Doe Updated")))
                .andExpect(jsonPath("$.email", is("john.updated@example.com")));
    }

    @Test
    void testUpdateCustomerNotFound() throws Exception {
        // Given
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

        given(customerService.updateCustomer(anyInt(), any(CustomerDto.class))).willReturn(Optional.empty());

        // When/Then
        mockMvc.perform(put("/api/v1/customers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerToUpdate)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteCustomer() throws Exception {
        // Given
        given(customerService.getCustomerById(1)).willReturn(Optional.of(testCustomer));
        doNothing().when(customerService).deleteCustomerById(1);

        // When/Then
        mockMvc.perform(delete("/api/v1/customers/1"))
                .andExpect(status().isNoContent());

        verify(customerService).deleteCustomerById(1);
    }

    @Test
    void testDeleteCustomerNotFound() throws Exception {
        // Given
        given(customerService.getCustomerById(1)).willReturn(Optional.empty());

        // When/Then
        mockMvc.perform(delete("/api/v1/customers/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testValidationErrors() throws Exception {
        // Given
        CustomerDto invalidCustomer = CustomerDto.builder()
                .email("invalid-email") // Invalid email format
                .phoneNumber("555-123-4567")
                // Missing required fields: name, addressLine1, city, state, postalCode
                .build();

        // When/Then
        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCustomer)))
                .andExpect(status().isBadRequest());
    }
}