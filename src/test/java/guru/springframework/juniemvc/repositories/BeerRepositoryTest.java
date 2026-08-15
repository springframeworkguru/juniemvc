package guru.springframework.juniemvc.repositories;

import guru.springframework.juniemvc.entities.Beer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BeerRepositoryTest {

    @Autowired
    BeerRepository beerRepository;

    @Test
    void testSaveBeer() {
        // Given
        Beer beer = Beer.builder()
                .beerName("Test Beer")
                .beerStyle("IPA")
                .upc("123456")
                .price(new BigDecimal("12.99"))
                .quantityOnHand(100)
                .build();

        // When
        Beer savedBeer = beerRepository.save(beer);

        // Then
        assertThat(savedBeer).isNotNull();
        assertThat(savedBeer.getId()).isNotNull();
    }

    @Test
    void testGetBeerById() {
        // Given
        Beer beer = Beer.builder()
                .beerName("Test Beer")
                .beerStyle("IPA")
                .upc("123456")
                .price(new BigDecimal("12.99"))
                .quantityOnHand(100)
                .build();
        Beer savedBeer = beerRepository.save(beer);

        // When
        Optional<Beer> fetchedBeerOptional = beerRepository.findById(savedBeer.getId());

        // Then
        assertThat(fetchedBeerOptional).isPresent();
        Beer fetchedBeer = fetchedBeerOptional.get();
        assertThat(fetchedBeer.getBeerName()).isEqualTo("Test Beer");
    }

    @Test
    void testUpdateBeer() {
        // Given
        Beer beer = Beer.builder()
                .beerName("Original Name")
                .beerStyle("IPA")
                .upc("123456")
                .price(new BigDecimal("12.99"))
                .quantityOnHand(100)
                .build();
        Beer savedBeer = beerRepository.save(beer);

        // When
        savedBeer.setBeerName("Updated Name");
        Beer updatedBeer = beerRepository.save(savedBeer);

        // Then
        assertThat(updatedBeer.getBeerName()).isEqualTo("Updated Name");
    }

    @Test
    void testDeleteBeer() {
        // Given
        Beer beer = Beer.builder()
                .beerName("Delete Me")
                .beerStyle("Lager")
                .upc("654321")
                .price(new BigDecimal("9.99"))
                .quantityOnHand(50)
                .build();
        Beer savedBeer = beerRepository.save(beer);

        // When
        beerRepository.deleteById(savedBeer.getId());
        Optional<Beer> deletedBeer = beerRepository.findById(savedBeer.getId());

        // Then
        assertThat(deletedBeer).isEmpty();
    }

    @Test
    void testListBeers() {
        // Given
        beerRepository.deleteAll(); // Clear any existing data
        Beer beer1 = Beer.builder()
                .beerName("Beer 1")
                .beerStyle("IPA")
                .upc("111111")
                .price(new BigDecimal("11.99"))
                .quantityOnHand(100)
                .build();
        Beer beer2 = Beer.builder()
                .beerName("Beer 2")
                .beerStyle("Stout")
                .upc("222222")
                .price(new BigDecimal("13.99"))
                .quantityOnHand(200)
                .build();
        beerRepository.saveAll(List.of(beer1, beer2));

        // When
        List<Beer> beers = beerRepository.findAll();

        // Then
        assertThat(beers).hasSize(2);
    }

    @Test
    void testFindAllByBeerNameContainingIgnoreCaseWithPagination() {
        // Given
        beerRepository.deleteAll(); // Clear any existing data
        Beer beer1 = Beer.builder()
                .beerName("Test Beer")
                .beerStyle("IPA")
                .upc("111111")
                .price(new BigDecimal("11.99"))
                .quantityOnHand(100)
                .build();
        Beer beer2 = Beer.builder()
                .beerName("Another Test Beer")
                .beerStyle("Stout")
                .upc("222222")
                .price(new BigDecimal("13.99"))
                .quantityOnHand(200)
                .build();
        Beer beer3 = Beer.builder()
                .beerName("Not Matching")
                .beerStyle("Lager")
                .upc("333333")
                .price(new BigDecimal("10.99"))
                .quantityOnHand(150)
                .build();
        beerRepository.saveAll(List.of(beer1, beer2, beer3));

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Beer> beersPage = beerRepository.findAllByBeerNameContainingIgnoreCase("Test", pageable);

        // Then
        assertThat(beersPage.getContent()).hasSize(2);
        assertThat(beersPage.getTotalElements()).isEqualTo(2);
        assertThat(beersPage.getContent().get(0).getBeerName()).contains("Test");
        assertThat(beersPage.getContent().get(1).getBeerName()).contains("Test");
    }

    @Test
    void testFindAllByBeerNameContainingIgnoreCaseWithEmptyString() {
        // Given
        beerRepository.deleteAll(); // Clear any existing data
        Beer beer1 = Beer.builder()
                .beerName("Beer 1")
                .beerStyle("IPA")
                .upc("111111")
                .price(new BigDecimal("11.99"))
                .quantityOnHand(100)
                .build();
        Beer beer2 = Beer.builder()
                .beerName("Beer 2")
                .beerStyle("Stout")
                .upc("222222")
                .price(new BigDecimal("13.99"))
                .quantityOnHand(200)
                .build();
        beerRepository.saveAll(List.of(beer1, beer2));

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Beer> beersPage = beerRepository.findAllByBeerNameContainingIgnoreCase("", pageable);

        // Then
        assertThat(beersPage.getContent()).hasSize(2);
        assertThat(beersPage.getTotalElements()).isEqualTo(2);
    }

    @Test
    void testFindAllByBeerNameContainingIgnoreCasePagination() {
        // Given
        beerRepository.deleteAll(); // Clear any existing data
        // Create 25 beers with "Test" in the name
        for (int i = 1; i <= 25; i++) {
            Beer beer = Beer.builder()
                    .beerName("Test Beer " + i)
                    .beerStyle(i % 2 == 0 ? "IPA" : "Stout")
                    .upc("1111" + i)
                    .price(new BigDecimal("11.99"))
                    .quantityOnHand(100 + i)
                    .build();
            beerRepository.save(beer);
        }

        // First page (0-based index)
        Pageable firstPageable = PageRequest.of(0, 10);
        Page<Beer> firstPage = beerRepository.findAllByBeerNameContainingIgnoreCase("Test", firstPageable);

        // Second page
        Pageable secondPageable = PageRequest.of(1, 10);
        Page<Beer> secondPage = beerRepository.findAllByBeerNameContainingIgnoreCase("Test", secondPageable);

        // Third page (should have only 5 items)
        Pageable thirdPageable = PageRequest.of(2, 10);
        Page<Beer> thirdPage = beerRepository.findAllByBeerNameContainingIgnoreCase("Test", thirdPageable);

        // Then
        assertThat(firstPage.getContent()).hasSize(10);
        assertThat(firstPage.getNumber()).isEqualTo(0);
        assertThat(firstPage.getTotalElements()).isEqualTo(25);
        assertThat(firstPage.getTotalPages()).isEqualTo(3);

        assertThat(secondPage.getContent()).hasSize(10);
        assertThat(secondPage.getNumber()).isEqualTo(1);

        assertThat(thirdPage.getContent()).hasSize(5);
        assertThat(thirdPage.getNumber()).isEqualTo(2);
    }

    @Test
    void testFindAllByBeerNameAndBeerStyleContainingIgnoreCase() {
        // Given
        beerRepository.deleteAll(); // Clear any existing data
        Beer beer1 = Beer.builder()
                .beerName("Test IPA")
                .beerStyle("IPA")
                .upc("111111")
                .price(new BigDecimal("11.99"))
                .quantityOnHand(100)
                .build();
        Beer beer2 = Beer.builder()
                .beerName("Test Stout")
                .beerStyle("Stout")
                .upc("222222")
                .price(new BigDecimal("13.99"))
                .quantityOnHand(200)
                .build();
        Beer beer3 = Beer.builder()
                .beerName("Another IPA")
                .beerStyle("IPA")
                .upc("333333")
                .price(new BigDecimal("10.99"))
                .quantityOnHand(150)
                .build();
        Beer beer4 = Beer.builder()
                .beerName("Not Matching")
                .beerStyle("Lager")
                .upc("444444")
                .price(new BigDecimal("9.99"))
                .quantityOnHand(120)
                .build();
        beerRepository.saveAll(List.of(beer1, beer2, beer3, beer4));

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Beer> beersPage = beerRepository.findAllByBeerNameContainingIgnoreCaseAndBeerStyleContainingIgnoreCase("Test", "IPA", pageable);

        // Then
        assertThat(beersPage.getContent()).hasSize(1);
        assertThat(beersPage.getTotalElements()).isEqualTo(1);
        assertThat(beersPage.getContent().get(0).getBeerName()).isEqualTo("Test IPA");
        assertThat(beersPage.getContent().get(0).getBeerStyle()).isEqualTo("IPA");
    }

    @Test
    void testFindAllByBeerNameAndBeerStyleContainingIgnoreCaseWithEmptyBeerName() {
        // Given
        beerRepository.deleteAll(); // Clear any existing data
        Beer beer1 = Beer.builder()
                .beerName("Test IPA")
                .beerStyle("IPA")
                .upc("111111")
                .price(new BigDecimal("11.99"))
                .quantityOnHand(100)
                .build();
        Beer beer2 = Beer.builder()
                .beerName("Another IPA")
                .beerStyle("IPA")
                .upc("222222")
                .price(new BigDecimal("13.99"))
                .quantityOnHand(200)
                .build();
        Beer beer3 = Beer.builder()
                .beerName("Test Stout")
                .beerStyle("Stout")
                .upc("333333")
                .price(new BigDecimal("10.99"))
                .quantityOnHand(150)
                .build();
        beerRepository.saveAll(List.of(beer1, beer2, beer3));

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Beer> beersPage = beerRepository.findAllByBeerNameContainingIgnoreCaseAndBeerStyleContainingIgnoreCase("", "IPA", pageable);

        // Then
        assertThat(beersPage.getContent()).hasSize(2);
        assertThat(beersPage.getTotalElements()).isEqualTo(2);
        assertThat(beersPage.getContent().get(0).getBeerStyle()).isEqualTo("IPA");
        assertThat(beersPage.getContent().get(1).getBeerStyle()).isEqualTo("IPA");
    }

    @Test
    void testFindAllByBeerNameAndBeerStyleContainingIgnoreCaseWithEmptyBeerStyle() {
        // Given
        beerRepository.deleteAll(); // Clear any existing data
        Beer beer1 = Beer.builder()
                .beerName("Test IPA")
                .beerStyle("IPA")
                .upc("111111")
                .price(new BigDecimal("11.99"))
                .quantityOnHand(100)
                .build();
        Beer beer2 = Beer.builder()
                .beerName("Test Stout")
                .beerStyle("Stout")
                .upc("222222")
                .price(new BigDecimal("13.99"))
                .quantityOnHand(200)
                .build();
        Beer beer3 = Beer.builder()
                .beerName("Another Beer")
                .beerStyle("Lager")
                .upc("333333")
                .price(new BigDecimal("10.99"))
                .quantityOnHand(150)
                .build();
        beerRepository.saveAll(List.of(beer1, beer2, beer3));

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Beer> beersPage = beerRepository.findAllByBeerNameContainingIgnoreCaseAndBeerStyleContainingIgnoreCase("Test", "", pageable);

        // Then
        assertThat(beersPage.getContent()).hasSize(2);
        assertThat(beersPage.getTotalElements()).isEqualTo(2);
        assertThat(beersPage.getContent().get(0).getBeerName()).contains("Test");
        assertThat(beersPage.getContent().get(1).getBeerName()).contains("Test");
    }

    @Test
    void testFindAllByBeerNameAndBeerStyleContainingIgnoreCaseWithBothEmpty() {
        // Given
        beerRepository.deleteAll(); // Clear any existing data
        Beer beer1 = Beer.builder()
                .beerName("Test IPA")
                .beerStyle("IPA")
                .upc("111111")
                .price(new BigDecimal("11.99"))
                .quantityOnHand(100)
                .build();
        Beer beer2 = Beer.builder()
                .beerName("Another Beer")
                .beerStyle("Stout")
                .upc("222222")
                .price(new BigDecimal("13.99"))
                .quantityOnHand(200)
                .build();
        beerRepository.saveAll(List.of(beer1, beer2));

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Beer> beersPage = beerRepository.findAllByBeerNameContainingIgnoreCaseAndBeerStyleContainingIgnoreCase("", "", pageable);

        // Then
        assertThat(beersPage.getContent()).hasSize(2);
        assertThat(beersPage.getTotalElements()).isEqualTo(2);
    }
}
