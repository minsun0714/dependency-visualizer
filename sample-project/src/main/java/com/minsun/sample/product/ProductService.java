package com.minsun.sample.product;

import com.minsun.sample.shared.PriceCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryService inventoryService;  // 👈 same-package: product -> product (SCC B 사이클)
    private final PriceCalculator priceCalculator;

    public Product find(Long id) {
        return productRepository.findById(id);
    }
}
