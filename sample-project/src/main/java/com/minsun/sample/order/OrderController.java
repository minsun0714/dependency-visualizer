package com.minsun.sample.order;

import com.minsun.sample.shared.BaseController;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController extends BaseController {

    private final OrderService orderService;

    @PostMapping("/orders")
    public Order create() {
        return orderService.createOrder();
    }
}
