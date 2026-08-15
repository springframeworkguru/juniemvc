package guru.springframework.juniemvc.repositories;

import guru.springframework.juniemvc.entities.Beer;
import guru.springframework.juniemvc.entities.BeerOrder;
import guru.springframework.juniemvc.entities.BeerOrderLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BeerOrderRepositoryTest {

    @Autowired
    BeerOrderRepository beerOrderRepository;

    @Autowired
    BeerRepository beerRepository;

    private Beer testBeer;

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
    }

    @Test
    void testSaveBeerOrder() {
        // Given
        BeerOrder beerOrder = BeerOrder.builder()
                .customerRef("Test Customer")
                .paymentAmount(new BigDecimal("25.98"))
                .status("NEW")
                .build();

        BeerOrderLine beerOrderLine = BeerOrderLine.builder()
                .orderQuantity(2)
                .quantityAllocated(0)
                .status("NEW")
                .beer(testBeer)
                .build();

        beerOrder.addBeerOrderLine(beerOrderLine);

        // When
        BeerOrder savedBeerOrder = beerOrderRepository.save(beerOrder);

        // Then
        assertThat(savedBeerOrder).isNotNull();
        assertThat(savedBeerOrder.getId()).isNotNull();
        assertThat(savedBeerOrder.getBeerOrderLines()).hasSize(1);
        assertThat(savedBeerOrder.getBeerOrderLines().iterator().next().getBeer().getId()).isEqualTo(testBeer.getId());
    }

    @Test
    void testGetBeerOrderById() {
        // Given
        BeerOrder beerOrder = BeerOrder.builder()
                .customerRef("Test Customer")
                .paymentAmount(new BigDecimal("25.98"))
                .status("NEW")
                .build();

        BeerOrderLine beerOrderLine = BeerOrderLine.builder()
                .orderQuantity(2)
                .quantityAllocated(0)
                .status("NEW")
                .beer(testBeer)
                .build();

        beerOrder.addBeerOrderLine(beerOrderLine);
        BeerOrder savedBeerOrder = beerOrderRepository.save(beerOrder);

        // When
        Optional<BeerOrder> fetchedBeerOrderOptional = beerOrderRepository.findById(savedBeerOrder.getId());

        // Then
        assertThat(fetchedBeerOrderOptional).isPresent();
        BeerOrder fetchedBeerOrder = fetchedBeerOrderOptional.get();
        assertThat(fetchedBeerOrder.getCustomerRef()).isEqualTo("Test Customer");
        assertThat(fetchedBeerOrder.getBeerOrderLines()).hasSize(1);
    }

    @Test
    void testUpdateBeerOrder() {
        // Given
        BeerOrder beerOrder = BeerOrder.builder()
                .customerRef("Original Customer")
                .paymentAmount(new BigDecimal("25.98"))
                .status("NEW")
                .build();

        BeerOrderLine beerOrderLine = BeerOrderLine.builder()
                .orderQuantity(2)
                .quantityAllocated(0)
                .status("NEW")
                .beer(testBeer)
                .build();

        beerOrder.addBeerOrderLine(beerOrderLine);
        BeerOrder savedBeerOrder = beerOrderRepository.save(beerOrder);

        // When
        savedBeerOrder.setCustomerRef("Updated Customer");
        savedBeerOrder.setStatus("PROCESSING");
        BeerOrder updatedBeerOrder = beerOrderRepository.save(savedBeerOrder);

        // Then
        assertThat(updatedBeerOrder.getCustomerRef()).isEqualTo("Updated Customer");
        assertThat(updatedBeerOrder.getStatus()).isEqualTo("PROCESSING");
    }

    @Test
    void testDeleteBeerOrder() {
        // Given
        BeerOrder beerOrder = BeerOrder.builder()
                .customerRef("Delete Me")
                .paymentAmount(new BigDecimal("25.98"))
                .status("NEW")
                .build();

        BeerOrderLine beerOrderLine = BeerOrderLine.builder()
                .orderQuantity(2)
                .quantityAllocated(0)
                .status("NEW")
                .beer(testBeer)
                .build();

        beerOrder.addBeerOrderLine(beerOrderLine);
        BeerOrder savedBeerOrder = beerOrderRepository.save(beerOrder);

        // When
        beerOrderRepository.deleteById(savedBeerOrder.getId());
        Optional<BeerOrder> deletedBeerOrder = beerOrderRepository.findById(savedBeerOrder.getId());

        // Then
        assertThat(deletedBeerOrder).isEmpty();
    }

    @Test
    void testListBeerOrders() {
        // Given
        beerOrderRepository.deleteAll(); // Clear any existing data

        BeerOrder beerOrder1 = BeerOrder.builder()
                .customerRef("Customer 1")
                .paymentAmount(new BigDecimal("25.98"))
                .status("NEW")
                .build();

        BeerOrderLine beerOrderLine1 = BeerOrderLine.builder()
                .orderQuantity(2)
                .quantityAllocated(0)
                .status("NEW")
                .beer(testBeer)
                .build();

        beerOrder1.addBeerOrderLine(beerOrderLine1);

        BeerOrder beerOrder2 = BeerOrder.builder()
                .customerRef("Customer 2")
                .paymentAmount(new BigDecimal("39.97"))
                .status("PROCESSING")
                .build();

        BeerOrderLine beerOrderLine2 = BeerOrderLine.builder()
                .orderQuantity(3)
                .quantityAllocated(0)
                .status("NEW")
                .beer(testBeer)
                .build();

        beerOrder2.addBeerOrderLine(beerOrderLine2);

        beerOrderRepository.saveAll(List.of(beerOrder1, beerOrder2));

        // When
        List<BeerOrder> beerOrders = beerOrderRepository.findAll();

        // Then
        assertThat(beerOrders).hasSize(2);
    }
}