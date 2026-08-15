package guru.springframework.juniemvc.controllers;

import guru.springframework.juniemvc.models.BeerOrderShipmentDto;
import guru.springframework.juniemvc.services.BeerOrderService;
import guru.springframework.juniemvc.services.BeerOrderShipmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for BeerOrderShipmentController
 */
@WebMvcTest(BeerOrderShipmentController.class)
class BeerOrderShipmentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    BeerOrderShipmentService beerOrderShipmentService;

    @MockitoBean
    BeerOrderService beerOrderService;

    BeerOrderShipmentDto testShipment;
    LocalDateTime testShipmentDate;

    @BeforeEach
    void setUp() {
        // Create test shipment date
        testShipmentDate = LocalDateTime.now();

        // Create test shipment
        testShipment = BeerOrderShipmentDto.builder()
                .id(1)
                .shipmentDate(testShipmentDate)
                .carrier("FedEx")
                .trackingNumber("123456789")
                .build();
    }

    @Test
    void testGetAllShipments() throws Exception {
        // Given
        List<BeerOrderShipmentDto> shipments = Arrays.asList(testShipment);
        given(beerOrderShipmentService.getAllShipments(1)).willReturn(shipments);

        // When/Then
        mockMvc.perform(get("/api/v1/beer-orders/1/shipments")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].carrier", is("FedEx")))
                .andExpect(jsonPath("$[0].trackingNumber", is("123456789")));
    }

    @Test
    void testGetShipmentById() throws Exception {
        // Given
        given(beerOrderShipmentService.getShipmentById(1, 1)).willReturn(testShipment);

        // When/Then
        mockMvc.perform(get("/api/v1/beer-orders/1/shipments/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.carrier", is("FedEx")))
                .andExpect(jsonPath("$.trackingNumber", is("123456789")));
    }

    @Test
    void testCreateShipment() throws Exception {
        // Given
        BeerOrderShipmentDto shipmentToCreate = BeerOrderShipmentDto.builder()
                .shipmentDate(testShipmentDate)
                .carrier("UPS")
                .trackingNumber("987654321")
                .build();

        BeerOrderShipmentDto createdShipment = BeerOrderShipmentDto.builder()
                .id(2)
                .shipmentDate(testShipmentDate)
                .carrier("UPS")
                .trackingNumber("987654321")
                .build();

        given(beerOrderShipmentService.createShipment(anyInt(), any(BeerOrderShipmentDto.class))).willReturn(createdShipment);

        // When/Then
        mockMvc.perform(post("/api/v1/beer-orders/1/shipments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shipmentToCreate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.carrier", is("UPS")))
                .andExpect(jsonPath("$.trackingNumber", is("987654321")));
    }

    @Test
    void testUpdateShipment() throws Exception {
        // Given
        BeerOrderShipmentDto shipmentToUpdate = BeerOrderShipmentDto.builder()
                .shipmentDate(testShipmentDate)
                .carrier("DHL")
                .trackingNumber("UPDATED123")
                .build();

        BeerOrderShipmentDto updatedShipment = BeerOrderShipmentDto.builder()
                .id(1)
                .shipmentDate(testShipmentDate)
                .carrier("DHL")
                .trackingNumber("UPDATED123")
                .build();

        given(beerOrderShipmentService.updateShipment(anyInt(), anyInt(), any(BeerOrderShipmentDto.class))).willReturn(updatedShipment);

        // When/Then
        mockMvc.perform(put("/api/v1/beer-orders/1/shipments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shipmentToUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.carrier", is("DHL")))
                .andExpect(jsonPath("$.trackingNumber", is("UPDATED123")));
    }

    @Test
    void testDeleteShipment() throws Exception {
        // Given
        doNothing().when(beerOrderShipmentService).deleteShipment(1, 1);

        // When/Then
        mockMvc.perform(delete("/api/v1/beer-orders/1/shipments/1"))
                .andExpect(status().isNoContent());

        verify(beerOrderShipmentService).deleteShipment(1, 1);
    }

    @Test
    void testValidationErrors() throws Exception {
        // Given
        BeerOrderShipmentDto invalidShipment = BeerOrderShipmentDto.builder()
                .carrier("FedEx")
                .trackingNumber("123456789")
                // Missing required shipmentDate
                .build();

        // When/Then
        mockMvc.perform(post("/api/v1/beer-orders/1/shipments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidShipment)))
                .andExpect(status().isBadRequest());
    }
}