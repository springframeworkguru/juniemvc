package guru.springframework.juniemvc.repositories;

import guru.springframework.juniemvc.entities.Beer;
import guru.springframework.juniemvc.entities.BeerOrder;
import guru.springframework.juniemvc.entities.BeerOrderLine;
import guru.springframework.juniemvc.entities.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BeerOrderLineRepositoryTest {

    @Autowired
    BeerOrderLineRepository beerOrderLineRepository;

    @Autowired
    BeerOrderRepository beerOrderRepository;

    @Autowired
    BeerRepository beerRepository;

    @Autowired
    CustomerRepository customerRepository;

    private Beer testBeer;
    private BeerOrder testBeerOrder;
    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        // Create and save a test beer
        testBeer = Beer.builder()
                .beerName("Test Beer")
                .beerStyle("IPA")
                .upc("123456")
                .price(new BigDecimal("12.99"))
                .quantityOnHand(100)
                .build();
        testBeer = beerRepository.save(testBeer);

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
    void testSaveBeerOrderLine() {
        // Given
        BeerOrderLine beerOrderLine = BeerOrderLine.builder()
                .orderQuantity(2)
                .quantityAllocated(0)
                .status("NEW")
                .beer(testBeer)
                .beerOrder(testBeerOrder)
                .build();

        // When
        BeerOrderLine savedBeerOrderLine = beerOrderLineRepository.save(beerOrderLine);

        // Then
        assertThat(savedBeerOrderLine).isNotNull();
        assertThat(savedBeerOrderLine.getId()).isNotNull();
        assertThat(savedBeerOrderLine.getBeer().getId()).isEqualTo(testBeer.getId());
        assertThat(savedBeerOrderLine.getBeerOrder().getId()).isEqualTo(testBeerOrder.getId());
    }

    @Test
    void testGetBeerOrderLineById() {
        // Given
        BeerOrderLine beerOrderLine = BeerOrderLine.builder()
                .orderQuantity(2)
                .quantityAllocated(0)
                .status("NEW")
                .beer(testBeer)
                .beerOrder(testBeerOrder)
                .build();
        BeerOrderLine savedBeerOrderLine = beerOrderLineRepository.save(beerOrderLine);

        // When
        Optional<BeerOrderLine> fetchedBeerOrderLineOptional = beerOrderLineRepository.findById(savedBeerOrderLine.getId());

        // Then
        assertThat(fetchedBeerOrderLineOptional).isPresent();
        BeerOrderLine fetchedBeerOrderLine = fetchedBeerOrderLineOptional.get();
        assertThat(fetchedBeerOrderLine.getOrderQuantity()).isEqualTo(2);
        assertThat(fetchedBeerOrderLine.getBeer().getId()).isEqualTo(testBeer.getId());
        assertThat(fetchedBeerOrderLine.getBeerOrder().getId()).isEqualTo(testBeerOrder.getId());
    }

    @Test
    void testUpdateBeerOrderLine() {
        // Given
        BeerOrderLine beerOrderLine = BeerOrderLine.builder()
                .orderQuantity(2)
                .quantityAllocated(0)
                .status("NEW")
                .beer(testBeer)
                .beerOrder(testBeerOrder)
                .build();
        BeerOrderLine savedBeerOrderLine = beerOrderLineRepository.save(beerOrderLine);

        // When
        savedBeerOrderLine.setOrderQuantity(3);
        savedBeerOrderLine.setQuantityAllocated(3);
        savedBeerOrderLine.setStatus("ALLOCATED");
        BeerOrderLine updatedBeerOrderLine = beerOrderLineRepository.save(savedBeerOrderLine);

        // Then
        assertThat(updatedBeerOrderLine.getOrderQuantity()).isEqualTo(3);
        assertThat(updatedBeerOrderLine.getQuantityAllocated()).isEqualTo(3);
        assertThat(updatedBeerOrderLine.getStatus()).isEqualTo("ALLOCATED");
    }

    @Test
    void testDeleteBeerOrderLine() {
        // Given
        BeerOrderLine beerOrderLine = BeerOrderLine.builder()
                .orderQuantity(2)
                .quantityAllocated(0)
                .status("NEW")
                .beer(testBeer)
                .beerOrder(testBeerOrder)
                .build();
        BeerOrderLine savedBeerOrderLine = beerOrderLineRepository.save(beerOrderLine);

        // When
        beerOrderLineRepository.deleteById(savedBeerOrderLine.getId());
        Optional<BeerOrderLine> deletedBeerOrderLine = beerOrderLineRepository.findById(savedBeerOrderLine.getId());

        // Then
        assertThat(deletedBeerOrderLine).isEmpty();
    }

    @Test
    void testListBeerOrderLines() {
        // Given
        beerOrderLineRepository.deleteAll(); // Clear any existing data

        BeerOrderLine beerOrderLine1 = BeerOrderLine.builder()
                .orderQuantity(2)
                .quantityAllocated(0)
                .status("NEW")
                .beer(testBeer)
                .beerOrder(testBeerOrder)
                .build();

        BeerOrderLine beerOrderLine2 = BeerOrderLine.builder()
                .orderQuantity(3)
                .quantityAllocated(0)
                .status("NEW")
                .beer(testBeer)
                .beerOrder(testBeerOrder)
                .build();

        beerOrderLineRepository.saveAll(List.of(beerOrderLine1, beerOrderLine2));

        // When
        List<BeerOrderLine> beerOrderLines = beerOrderLineRepository.findAll();

        // Then
        assertThat(beerOrderLines).hasSize(2);
    }
}
