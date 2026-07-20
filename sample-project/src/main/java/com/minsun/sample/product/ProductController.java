package com.minsun.sample.product;

import com.minsun.sample.shared.BaseController;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProductController extends BaseController {

    private final ProductService productService;

    @GetMapping("/products")
    public Product get() {
        return productService.find(1L);
    }
}
