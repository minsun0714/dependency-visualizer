package com.minsun.sample.order;

import org.springframework.stereotype.Repository;

@Repository
public class OrderRepository {

    public Order save(Order order) {
        return order;
    }

    public int countByUser(Long userId) {
        return 0;
    }
}
