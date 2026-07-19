package com.minsun.sample.product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryService {

    // 👇 ProductService 는 같은 패키지라 import 문이 없다!
    //    import 기반 추출이면 이 간선을 놓치고, SymbolSolver 만 잡아낸다. (SCC B 사이클 완성)
    private final ProductService productService;
    private final ProductRepository productRepository;

    public int stockOf(Long productId) {
        return productRepository.findById(productId) == null ? 0 : 10;
    }
}
