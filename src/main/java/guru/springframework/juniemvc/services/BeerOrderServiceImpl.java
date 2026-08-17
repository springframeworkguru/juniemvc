package guru.springframework.juniemvc.services;

import guru.springframework.juniemvc.entities.Beer;
import guru.springframework.juniemvc.entities.BeerOrder;
import guru.springframework.juniemvc.entities.BeerOrderLine;
import guru.springframework.juniemvc.entities.Customer;
import guru.springframework.juniemvc.exceptions.NotFoundException;
import guru.springframework.juniemvc.mappers.BeerOrderLineMapper;
import guru.springframework.juniemvc.mappers.BeerOrderMapper;
import guru.springframework.juniemvc.models.BeerOrderDto;
import guru.springframework.juniemvc.repositories.BeerOrderRepository;
import guru.springframework.juniemvc.repositories.BeerRepository;
import guru.springframework.juniemvc.repositories.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of BeerOrderService that uses BeerOrderRepository for persistence
 */
@Service
public class BeerOrderServiceImpl implements BeerOrderService {

    private final BeerOrderRepository beerOrderRepository;
    private final BeerRepository beerRepository;
    private final CustomerRepository customerRepository;
    private final BeerOrderMapper beerOrderMapper;
    private final BeerOrderLineMapper beerOrderLineMapper;

    public BeerOrderServiceImpl(BeerOrderRepository beerOrderRepository,
                               BeerRepository beerRepository,
                               CustomerRepository customerRepository,
                               BeerOrderMapper beerOrderMapper,
                               BeerOrderLineMapper beerOrderLineMapper) {
        this.beerOrderRepository = beerOrderRepository;
        this.beerRepository = beerRepository;
        this.customerRepository = customerRepository;
        this.beerOrderMapper = beerOrderMapper;
        this.beerOrderLineMapper = beerOrderLineMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BeerOrderDto> getAllBeerOrders() {
        return beerOrderRepository.findAll().stream()
                .map(beerOrderMapper::beerOrderToBeerOrderDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BeerOrderDto> getBeerOrderById(Integer id) {
        return beerOrderRepository.findById(id)
                .map(beerOrderMapper::beerOrderToBeerOrderDto);
    }

    @Override
    @Transactional
    public BeerOrderDto saveBeerOrder(BeerOrderDto beerOrderDto) {
        BeerOrder beerOrder = beerOrderMapper.beerOrderDtoToBeerOrder(beerOrderDto);

        // Attach a managed Customer reference (avoid detached/transient customer on save)
        if (beerOrderDto.getCustomer() == null || beerOrderDto.getCustomer().getId() == null) {
            throw new NotFoundException("Customer is required");
        }
        Customer customer = customerRepository.findById(beerOrderDto.getCustomer().getId())
                .orElseThrow(() -> new NotFoundException("Customer not found with id: "
                        + beerOrderDto.getCustomer().getId()));
        beerOrder.setCustomer(customer);

        // Replace lines so updates do not keep stale collections from the mapper
        beerOrder.setBeerOrderLines(new HashSet<>());

        if (beerOrderDto.getBeerOrderLines() != null) {
            beerOrderDto.getBeerOrderLines().forEach(lineDto -> {
                BeerOrderLine line = beerOrderLineMapper.beerOrderLineDtoToBeerOrderLine(lineDto);

                if (lineDto.getBeerId() != null) {
                    Beer beer = beerRepository.findById(lineDto.getBeerId())
                            .orElseThrow(() -> new NotFoundException("Beer not found with id: "
                                    + lineDto.getBeerId()));
                    line.setBeer(beer);
                }

                beerOrder.addBeerOrderLine(line);
            });
        }

        BeerOrder savedBeerOrder = beerOrderRepository.save(beerOrder);
        return beerOrderMapper.beerOrderToBeerOrderDto(savedBeerOrder);
    }

    @Override
    @Transactional
    public void deleteBeerOrderById(Integer id) {
        beerOrderRepository.deleteById(id);
    }
}