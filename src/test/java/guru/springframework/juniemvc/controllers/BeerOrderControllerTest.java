package guru.springframework.juniemvc.controllers;

import guru.springframework.juniemvc.models.BeerOrderDto;
import guru.springframework.juniemvc.models.BeerOrderLineDto;
import guru.springframework.juniemvc.models.CustomerDto;
import guru.springframework.juniemvc.services.BeerOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BeerOrderController.class)
class BeerOrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    BeerOrderService beerOrderService;

    BeerOrderDto testBeerOrder;
    BeerOrderLineDto testBeerOrderLine;
    CustomerDto testCustomerDto;

    @BeforeEach
    void setUp() {
        // Create test customer DTO
        testCustomerDto = CustomerDto.builder()
                .id(1)
                .name("Test Customer")
                .email("test@example.com")
                .phoneNumber("555-123-4567")
                .addressLine1("123 Main St")
                .city("Springfield")
                .state("IL")
                .postalCode("62701")
                .build();

        // Create test beer order line
        testBeerOrderLine = BeerOrderLineDto.builder()
                .id(1)
                .beerId(1)
                .beerName("Test Beer")
                .beerStyle("IPA")
                .upc("123456")
                .orderQuantity(2)
                .quantityAllocated(2)
                .status("ALLOCATED")
                .build();

        // Create test beer order
        Set<BeerOrderLineDto> lines = new HashSet<>();
        lines.add(testBeerOrderLine);
        testBeerOrder = BeerOrderDto.builder()
                .id(1)
                .customer(testCustomerDto)
                .paymentAmount(new BigDecimal("25.98"))
                .status("NEW")
                .beerOrderLines(lines)
                .build();
    }

    @Test
    void testGetAllBeerOrders() throws Exception {
        // Given
        given(beerOrderService.getAllBeerOrders()).willReturn(Arrays.asList(testBeerOrder));

        // When/Then
        mockMvc.perform(get("/api/v1/beer-orders")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].customer.name", is("Test Customer")));
    }

    @Test
    void testGetBeerOrderById() throws Exception {
        // Given
        given(beerOrderService.getBeerOrderById(1)).willReturn(Optional.of(testBeerOrder));

        // When/Then
        mockMvc.perform(get("/api/v1/beer-orders/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.customer.name", is("Test Customer")))
                .andExpect(jsonPath("$.beerOrderLines", hasSize(1)));
    }

    @Test
    void testGetBeerOrderByIdNotFound() throws Exception {
        // Given
        given(beerOrderService.getBeerOrderById(1)).willReturn(Optional.empty());

        // When/Then
        mockMvc.perform(get("/api/v1/beer-orders/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateBeerOrder() throws Exception {
        // Given
        CustomerDto newCustomerDto = CustomerDto.builder()
                .id(2)
                .name("New Customer")
                .email("new@example.com")
                .phoneNumber("555-987-6543")
                .addressLine1("456 Oak Ave")
                .city("Shelbyville")
                .state("IL")
                .postalCode("62565")
                .build();

        BeerOrderDto beerOrderToCreate = BeerOrderDto.builder()
                .customer(newCustomerDto)
                .paymentAmount(new BigDecimal("39.97"))
                .status("NEW")
                .beerOrderLines(new HashSet<>())
                .build();

        BeerOrderLineDto lineDto = BeerOrderLineDto.builder()
                .beerId(1)
                .beerName("Test Beer")
                .beerStyle("IPA")
                .upc("123456")
                .orderQuantity(3)
                .quantityAllocated(0)
                .status("NEW")
                .build();

        beerOrderToCreate.getBeerOrderLines().add(lineDto);

        BeerOrderDto savedBeerOrder = BeerOrderDto.builder()
                .id(2)
                .customer(newCustomerDto)
                .paymentAmount(new BigDecimal("39.97"))
                .status("NEW")
                .beerOrderLines(beerOrderToCreate.getBeerOrderLines())
                .build();

        given(beerOrderService.saveBeerOrder(any(BeerOrderDto.class))).willReturn(savedBeerOrder);

        // When/Then
        mockMvc.perform(post("/api/v1/beer-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(beerOrderToCreate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.customer.name", is("New Customer")));
    }

    @Test
    void testUpdateBeerOrder() throws Exception {
        // Given
        CustomerDto updatedCustomerDto = CustomerDto.builder()
                .id(1)
                .name("Updated Customer")
                .email("updated@example.com")
                .phoneNumber("555-111-2222")
                .addressLine1("789 Pine St")
                .city("Capital City")
                .state("IL")
                .postalCode("62701")
                .build();

        BeerOrderDto beerOrderToUpdate = BeerOrderDto.builder()
                .customer(updatedCustomerDto)
                .paymentAmount(new BigDecimal("39.97"))
                .status("PROCESSING")
                .beerOrderLines(new HashSet<>())
                .build();

        BeerOrderLineDto lineDto = BeerOrderLineDto.builder()
                .beerId(1)
                .beerName("Test Beer")
                .beerStyle("IPA")
                .upc("123456")
                .orderQuantity(3)
                .quantityAllocated(3)
                .status("ALLOCATED")
                .build();

        beerOrderToUpdate.getBeerOrderLines().add(lineDto);

        BeerOrderDto updatedBeerOrder = BeerOrderDto.builder()
                .id(1)
                .customer(updatedCustomerDto)
                .paymentAmount(new BigDecimal("39.97"))
                .status("PROCESSING")
                .beerOrderLines(beerOrderToUpdate.getBeerOrderLines())
                .build();

        given(beerOrderService.getBeerOrderById(1)).willReturn(Optional.of(testBeerOrder));
        given(beerOrderService.saveBeerOrder(any(BeerOrderDto.class))).willReturn(updatedBeerOrder);

        // When/Then
        mockMvc.perform(put("/api/v1/beer-orders/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(beerOrderToUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.customer.name", is("Updated Customer")))
                .andExpect(jsonPath("$.status", is("PROCESSING")));
    }

    @Test
    void testUpdateBeerOrderNotFound() throws Exception {
        // Given
        CustomerDto updatedCustomerDto = CustomerDto.builder()
                .id(1)
                .name("Updated Customer")
                .email("updated@example.com")
                .phoneNumber("555-111-2222")
                .addressLine1("789 Pine St")
                .city("Capital City")
                .state("IL")
                .postalCode("62701")
                .build();

        BeerOrderDto beerOrderToUpdate = BeerOrderDto.builder()
                .customer(updatedCustomerDto)
                .paymentAmount(new BigDecimal("39.97"))
                .status("PROCESSING")
                .beerOrderLines(new HashSet<>())
                .build();

        // Add a beer order line to pass validation
        BeerOrderLineDto lineDto = BeerOrderLineDto.builder()
                .beerId(1)
                .beerName("Test Beer")
                .beerStyle("IPA")
                .upc("123456")
                .orderQuantity(3)
                .quantityAllocated(0)
                .status("NEW")
                .build();
        beerOrderToUpdate.getBeerOrderLines().add(lineDto);

        given(beerOrderService.getBeerOrderById(1)).willReturn(Optional.empty());

        // When/Then
        mockMvc.perform(put("/api/v1/beer-orders/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(beerOrderToUpdate)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteBeerOrder() throws Exception {
        // Given
        given(beerOrderService.getBeerOrderById(1)).willReturn(Optional.of(testBeerOrder));
        doNothing().when(beerOrderService).deleteBeerOrderById(1);

        // When/Then
        mockMvc.perform(delete("/api/v1/beer-orders/1"))
                .andExpect(status().isNoContent());

        verify(beerOrderService).deleteBeerOrderById(1);
    }

    @Test
    void testDeleteBeerOrderNotFound() throws Exception {
        // Given
        given(beerOrderService.getBeerOrderById(1)).willReturn(Optional.empty());

        // When/Then
        mockMvc.perform(delete("/api/v1/beer-orders/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testValidationErrors() throws Exception {
        // Given
        CustomerDto invalidCustomerDto = CustomerDto.builder()
                .id(3)
                .name("Invalid Customer")
                .email("invalid@example.com")
                .phoneNumber("555-333-4444")
                .addressLine1("999 Invalid St")
                .city("Invalid City")
                .state("XX")
                .postalCode("99999")
                .build();

        BeerOrderDto invalidBeerOrder = BeerOrderDto.builder()
                .customer(invalidCustomerDto)
                .paymentAmount(new BigDecimal("-10.00")) // Invalid: negative amount
                .status("NEW")
                .beerOrderLines(new HashSet<>()) // Invalid: empty order lines
                .build();

        // When/Then
        mockMvc.perform(post("/api/v1/beer-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBeerOrder)))
                .andExpect(status().isBadRequest());
    }
}
