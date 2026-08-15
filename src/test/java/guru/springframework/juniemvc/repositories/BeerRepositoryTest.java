package guru.springframework.juniemvc.repositories;

import guru.springframework.juniemvc.entities.Beer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

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
}