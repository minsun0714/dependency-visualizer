package com.minsun.sample.product;

import org.springframework.stereotype.Repository;

@Repository
public class ProductRepository {

    public Product findById(Long id) {
        return new Product(id);
    }
}
