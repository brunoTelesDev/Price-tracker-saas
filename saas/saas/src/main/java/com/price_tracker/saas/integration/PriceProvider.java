package com.price_tracker.saas.integration;

import java.math.BigDecimal;

public interface PriceProvider {

    BigDecimal buscarPreco(String url);

}