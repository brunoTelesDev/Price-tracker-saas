package com.price_tracker.saas.service;

import com.price_tracker.saas.integration.amazon.AmazonProvider;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ScraperService {

    private final AmazonProvider amazonProvider;

    public ScraperService(AmazonProvider amazonProvider) {
        this.amazonProvider = amazonProvider;
    }

    public BigDecimal buscarPreco(String url) {

        if (!(url.contains("amazon.") || url.contains("a.co"))) {
            throw new IllegalArgumentException(
                    "Nesta versão apenas produtos da Amazon são suportados."
            );
        }

        return amazonProvider.buscarPreco(url);
    }
}