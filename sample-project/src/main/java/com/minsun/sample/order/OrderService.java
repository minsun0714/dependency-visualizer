package com.minsun.sample.order;

import com.minsun.sample.product.ProductService;
import com.minsun.sample.shared.PriceCalculator;
import com.minsun.sample.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;         // 👈 cross-domain: order -> user  (SCC A 사이클 완성: user <-> order)
    private final ProductService productService;   // 👈 cross-domain: order -> product (사이클 아님, 단방향)
    private final PriceCalculator priceCalculator;

    public Order createOrder() {
        return orderRepository.save(new Order(1L));
    }

    public int countByUser(Long userId) {
        return orderRepository.countByUser(userId);
    }
}
