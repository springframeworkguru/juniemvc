package guru.springframework.juniemvc.services;

import guru.springframework.juniemvc.entities.Beer;
import guru.springframework.juniemvc.entities.BeerOrder;
import guru.springframework.juniemvc.entities.BeerOrderLine;
import guru.springframework.juniemvc.entities.Customer;
import guru.springframework.juniemvc.mappers.BeerOrderLineMapper;
import guru.springframework.juniemvc.mappers.BeerOrderMapper;
import guru.springframework.juniemvc.models.BeerOrderDto;
import guru.springframework.juniemvc.models.BeerOrderLineDto;
import guru.springframework.juniemvc.models.CustomerDto;
import guru.springframework.juniemvc.repositories.BeerOrderRepository;
import guru.springframework.juniemvc.repositories.BeerRepository;
import guru.springframework.juniemvc.repositories.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BeerOrderServiceImplTest {

    @Mock
    BeerOrderRepository beerOrderRepository;

    @Mock
    BeerRepository beerRepository;

    @Mock
    CustomerRepository customerRepository;

    @Mock
    BeerOrderMapper beerOrderMapper;

    @Mock
    BeerOrderLineMapper beerOrderLineMapper;

    @InjectMocks
    BeerOrderServiceImpl beerOrderService;

    BeerOrder testBeerOrder;
    BeerOrderDto testBeerOrderDto;
    Beer testBeer;
    BeerOrderLine testBeerOrderLine;
    BeerOrderLineDto testBeerOrderLineDto;
    Customer testCustomer;
    CustomerDto testCustomerDto;

    @BeforeEach
    void setUp() {
        // Create test beer
        testBeer = Beer.builder()
                .beerName("Test Beer")
                .beerStyle("IPA")
                .upc("123456")
                .price(new BigDecimal("12.99"))
                .quantityOnHand(100)
                .build();
        testBeer.setId(1);

        // Create test customer
        testCustomer = Customer.builder()
                .name("Test Customer")
                .email("test@example.com")
                .phoneNumber("555-123-4567")
                .addressLine1("123 Main St")
                .city("Springfield")
                .state("IL")
                .postalCode("62701")
                .build();
        testCustomer.setId(1);

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
        testBeerOrderLine = BeerOrderLine.builder()
                .orderQuantity(2)
                .quantityAllocated(2)
                .status("ALLOCATED")
                .beer(testBeer)
                .build();
        testBeerOrderLine.setId(1);

        // Create test beer order
        testBeerOrder = BeerOrder.builder()
                .customer(testCustomer)
                .paymentAmount(new BigDecimal("25.98"))
                .status("NEW")
                .build();
        testBeerOrder.setId(1);
        testBeerOrder.addBeerOrderLine(testBeerOrderLine);

        // Create test beer order line DTO
        testBeerOrderLineDto = BeerOrderLineDto.builder()
                .id(1)
                .beerId(1)
                .beerName("Test Beer")
                .beerStyle("IPA")
                .upc("123456")
                .orderQuantity(2)
                .quantityAllocated(2)
                .status("ALLOCATED")
                .build();

        // Create test beer order DTO
        Set<BeerOrderLineDto> lines = new HashSet<>();
        lines.add(testBeerOrderLineDto);
        testBeerOrderDto = BeerOrderDto.builder()
                .id(1)
                .customer(testCustomerDto)
                .paymentAmount(new BigDecimal("25.98"))
                .status("NEW")
                .beerOrderLines(lines)
                .build();
    }

    @Test
    void getAllBeerOrders() {
        // Given
        when(beerOrderRepository.findAll()).thenReturn(Arrays.asList(testBeerOrder));
        when(beerOrderMapper.beerOrderToBeerOrderDto(testBeerOrder)).thenReturn(testBeerOrderDto);

        // When
        List<BeerOrderDto> beerOrders = beerOrderService.getAllBeerOrders();

        // Then
        assertThat(beerOrders).hasSize(1);
        assertThat(beerOrders.get(0).getCustomer()).isNotNull();
        assertThat(beerOrders.get(0).getCustomer().getName()).isEqualTo("Test Customer");
        verify(beerOrderRepository, times(1)).findAll();
        verify(beerOrderMapper, times(1)).beerOrderToBeerOrderDto(any(BeerOrder.class));
    }

    @Test
    void getBeerOrderById() {
        // Given
        when(beerOrderRepository.findById(1)).thenReturn(Optional.of(testBeerOrder));
        when(beerOrderMapper.beerOrderToBeerOrderDto(testBeerOrder)).thenReturn(testBeerOrderDto);

        // When
        Optional<BeerOrderDto> beerOrderOptional = beerOrderService.getBeerOrderById(1);

        // Then
        assertThat(beerOrderOptional).isPresent();
        assertThat(beerOrderOptional.get().getCustomer()).isNotNull();
        assertThat(beerOrderOptional.get().getCustomer().getName()).isEqualTo("Test Customer");
        verify(beerOrderRepository, times(1)).findById(1);
        verify(beerOrderMapper, times(1)).beerOrderToBeerOrderDto(any(BeerOrder.class));
    }

    @Test
    void getBeerOrderByIdNotFound() {
        // Given
        when(beerOrderRepository.findById(1)).thenReturn(Optional.empty());

        // When
        Optional<BeerOrderDto> beerOrderOptional = beerOrderService.getBeerOrderById(1);

        // Then
        assertThat(beerOrderOptional).isEmpty();
        verify(beerOrderRepository, times(1)).findById(1);
    }

    @Test
    void saveBeerOrder() {
        // Given
        when(beerOrderMapper.beerOrderDtoToBeerOrder(testBeerOrderDto)).thenReturn(testBeerOrder);
        when(customerRepository.findById(1)).thenReturn(Optional.of(testCustomer));
        when(beerOrderLineMapper.beerOrderLineDtoToBeerOrderLine(any(BeerOrderLineDto.class))).thenReturn(testBeerOrderLine);
        when(beerRepository.findById(1)).thenReturn(Optional.of(testBeer));
        when(beerOrderRepository.save(any(BeerOrder.class))).thenReturn(testBeerOrder);
        when(beerOrderMapper.beerOrderToBeerOrderDto(testBeerOrder)).thenReturn(testBeerOrderDto);

        // When
        BeerOrderDto savedBeerOrderDto = beerOrderService.saveBeerOrder(testBeerOrderDto);

        // Then
        assertThat(savedBeerOrderDto).isNotNull();
        assertThat(savedBeerOrderDto.getCustomer()).isNotNull();
        assertThat(savedBeerOrderDto.getCustomer().getName()).isEqualTo("Test Customer");
        verify(beerOrderMapper, times(1)).beerOrderDtoToBeerOrder(any(BeerOrderDto.class));
        verify(customerRepository, times(1)).findById(1);
        verify(beerOrderRepository, times(1)).save(any(BeerOrder.class));
        verify(beerOrderMapper, times(1)).beerOrderToBeerOrderDto(any(BeerOrder.class));
    }

    @Test
    void deleteBeerOrderById() {
        // When
        beerOrderService.deleteBeerOrderById(1);

        // Then
        verify(beerOrderRepository, times(1)).deleteById(1);
    }
}
