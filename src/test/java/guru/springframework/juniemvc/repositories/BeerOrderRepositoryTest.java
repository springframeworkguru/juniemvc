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
class BeerOrderRepositoryTest {

    @Autowired
    BeerOrderRepository beerOrderRepository;

    @Autowired
    BeerRepository beerRepository;

    @Autowired
    CustomerRepository customerRepository;

    private Beer testBeer;
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
    }

    @Test
    void testSaveBeerOrder() {
        // Given
        BeerOrder beerOrder = BeerOrder.builder()
                .customer(testCustomer)
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
                .customer(testCustomer)
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
        assertThat(fetchedBeerOrder.getCustomer()).isNotNull();
        assertThat(fetchedBeerOrder.getCustomer().getName()).isEqualTo(testCustomer.getName());
        assertThat(fetchedBeerOrder.getBeerOrderLines()).hasSize(1);
    }

    @Test
    void testUpdateBeerOrder() {
        // Given
        BeerOrder beerOrder = BeerOrder.builder()
                .customer(testCustomer)
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
        // Create a new customer for the update
        Customer updatedCustomer = Customer.builder()
                .name("Updated Customer")
                .email("updated@example.com")
                .phoneNumber("555-987-6543")
                .addressLine1("456 Oak Ave")
                .city("Shelbyville")
                .state("IL")
                .postalCode("62565")
                .build();
        updatedCustomer = customerRepository.save(updatedCustomer);

        savedBeerOrder.setCustomer(updatedCustomer);
        savedBeerOrder.setStatus("PROCESSING");
        BeerOrder updatedBeerOrder = beerOrderRepository.save(savedBeerOrder);

        // Then
        assertThat(updatedBeerOrder.getCustomer()).isNotNull();
        assertThat(updatedBeerOrder.getCustomer().getName()).isEqualTo("Updated Customer");
        assertThat(updatedBeerOrder.getStatus()).isEqualTo("PROCESSING");
    }

    @Test
    void testDeleteBeerOrder() {
        // Given
        BeerOrder beerOrder = BeerOrder.builder()
                .customer(testCustomer)
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
        customerRepository.deleteAll(); // Clear any existing customers

        // Create two test customers
        Customer customer1 = Customer.builder()
                .name("Customer 1")
                .email("customer1@example.com")
                .phoneNumber("555-111-1111")
                .addressLine1("111 First St")
                .city("Springfield")
                .state("IL")
                .postalCode("62701")
                .build();
        customer1 = customerRepository.save(customer1);

        Customer customer2 = Customer.builder()
                .name("Customer 2")
                .email("customer2@example.com")
                .phoneNumber("555-222-2222")
                .addressLine1("222 Second St")
                .city("Shelbyville")
                .state("IL")
                .postalCode("62565")
                .build();
        customer2 = customerRepository.save(customer2);

        BeerOrder beerOrder1 = BeerOrder.builder()
                .customer(customer1)
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
                .customer(customer2)
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
