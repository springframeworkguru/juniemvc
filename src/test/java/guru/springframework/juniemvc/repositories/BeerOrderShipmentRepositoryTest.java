package guru.springframework.juniemvc.repositories;

import guru.springframework.juniemvc.entities.BeerOrder;
import guru.springframework.juniemvc.entities.BeerOrderShipment;
import guru.springframework.juniemvc.entities.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for BeerOrderShipmentRepository
 */
@DataJpaTest
class BeerOrderShipmentRepositoryTest {

    @Autowired
    BeerOrderShipmentRepository beerOrderShipmentRepository;

    @Autowired
    BeerOrderRepository beerOrderRepository;

    @Autowired
    CustomerRepository customerRepository;

    private BeerOrder testBeerOrder;
    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        // Create and save a test customer
        testCustomer = Customer.builder()
                .name("Test Customer")
                .email("test@example.com")
                .phoneNumber("555-123-4567")
                .addressLine1("123 Main St")
                .city("Springfield")
                .state("IL")
                .postalCode("62701")
                .build();
        testCustomer = customerRepository.save(testCustomer);

        // Create and save a test beer order
        testBeerOrder = BeerOrder.builder()
                .customer(testCustomer)
                .paymentAmount(new BigDecimal("25.98"))
                .status("NEW")
                .build();
        testBeerOrder = beerOrderRepository.save(testBeerOrder);
    }

    @Test
    void testSaveBeerOrderShipment() {
        // Given
        BeerOrderShipment shipment = BeerOrderShipment.builder()
                .shipmentDate(LocalDateTime.now())
                .carrier("FedEx")
                .trackingNumber("123456789")
                .build();
        testBeerOrder.addShipment(shipment);

        // When
        BeerOrderShipment savedShipment = beerOrderShipmentRepository.save(shipment);

        // Then
        assertThat(savedShipment).isNotNull();
        assertThat(savedShipment.getId()).isNotNull();
        assertThat(savedShipment.getBeerOrder().getId()).isEqualTo(testBeerOrder.getId());
        assertThat(savedShipment.getCarrier()).isEqualTo("FedEx");
        assertThat(savedShipment.getTrackingNumber()).isEqualTo("123456789");
    }

    @Test
    void testGetBeerOrderShipmentById() {
        // Given
        BeerOrderShipment shipment = BeerOrderShipment.builder()
                .shipmentDate(LocalDateTime.now())
                .carrier("UPS")
                .trackingNumber("987654321")
                .build();
        testBeerOrder.addShipment(shipment);
        BeerOrderShipment savedShipment = beerOrderShipmentRepository.save(shipment);

        // When
        Optional<BeerOrderShipment> fetchedShipmentOptional = beerOrderShipmentRepository.findById(savedShipment.getId());

        // Then
        assertThat(fetchedShipmentOptional).isPresent();
        BeerOrderShipment fetchedShipment = fetchedShipmentOptional.get();
        assertThat(fetchedShipment.getBeerOrder()).isNotNull();
        assertThat(fetchedShipment.getBeerOrder().getId()).isEqualTo(testBeerOrder.getId());
        assertThat(fetchedShipment.getCarrier()).isEqualTo("UPS");
        assertThat(fetchedShipment.getTrackingNumber()).isEqualTo("987654321");
    }

    @Test
    void testUpdateBeerOrderShipment() {
        // Given
        BeerOrderShipment shipment = BeerOrderShipment.builder()
                .shipmentDate(LocalDateTime.now())
                .carrier("DHL")
                .trackingNumber("ABCDEF123")
                .build();
        testBeerOrder.addShipment(shipment);
        BeerOrderShipment savedShipment = beerOrderShipmentRepository.save(shipment);

        // When
        savedShipment.setCarrier("USPS");
        savedShipment.setTrackingNumber("UPDATED123");
        BeerOrderShipment updatedShipment = beerOrderShipmentRepository.save(savedShipment);

        // Then
        assertThat(updatedShipment.getCarrier()).isEqualTo("USPS");
        assertThat(updatedShipment.getTrackingNumber()).isEqualTo("UPDATED123");
    }

    @Test
    void testDeleteBeerOrderShipment() {
        // Given
        BeerOrderShipment shipment = BeerOrderShipment.builder()
                .shipmentDate(LocalDateTime.now())
                .carrier("Amazon")
                .trackingNumber("AMAZON123")
                .build();
        testBeerOrder.addShipment(shipment);
        BeerOrderShipment savedShipment = beerOrderShipmentRepository.save(shipment);

        // When
        beerOrderShipmentRepository.deleteById(savedShipment.getId());
        Optional<BeerOrderShipment> deletedShipment = beerOrderShipmentRepository.findById(savedShipment.getId());

        // Then
        assertThat(deletedShipment).isEmpty();
    }

    @Test
    void testFindByBeerOrderId() {
        // Given
        beerOrderShipmentRepository.deleteAll(); // Clear any existing data

        BeerOrderShipment shipment1 = BeerOrderShipment.builder()
                .shipmentDate(LocalDateTime.now())
                .carrier("FedEx")
                .trackingNumber("FEDEX123")
                .build();
        testBeerOrder.addShipment(shipment1);
        beerOrderShipmentRepository.save(shipment1);

        BeerOrderShipment shipment2 = BeerOrderShipment.builder()
                .shipmentDate(LocalDateTime.now().plusDays(1))
                .carrier("UPS")
                .trackingNumber("UPS456")
                .build();
        testBeerOrder.addShipment(shipment2);
        beerOrderShipmentRepository.save(shipment2);

        // Create another beer order with a shipment
        BeerOrder anotherBeerOrder = BeerOrder.builder()
                .customer(testCustomer)
                .paymentAmount(new BigDecimal("50.00"))
                .status("NEW")
                .build();
        anotherBeerOrder = beerOrderRepository.save(anotherBeerOrder);

        BeerOrderShipment shipment3 = BeerOrderShipment.builder()
                .shipmentDate(LocalDateTime.now())
                .carrier("DHL")
                .trackingNumber("DHL789")
                .build();
        anotherBeerOrder.addShipment(shipment3);
        beerOrderShipmentRepository.save(shipment3);

        // When
        List<BeerOrderShipment> shipments = beerOrderShipmentRepository.findByBeerOrderId(testBeerOrder.getId());

        // Then
        assertThat(shipments).hasSize(2);
        assertThat(shipments.get(0).getBeerOrder().getId()).isEqualTo(testBeerOrder.getId());
        assertThat(shipments.get(1).getBeerOrder().getId()).isEqualTo(testBeerOrder.getId());
    }
}