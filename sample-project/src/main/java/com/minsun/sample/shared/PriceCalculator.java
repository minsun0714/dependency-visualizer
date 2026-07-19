package com.minsun.sample.shared;

import org.springframework.stereotype.Component;

@Component
public class PriceCalculator {

    public long total(long unitPrice, int quantity) {
        return unitPrice * quantity;
    }
}
