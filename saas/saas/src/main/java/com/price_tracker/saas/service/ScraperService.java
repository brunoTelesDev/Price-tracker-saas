package com.price_tracker.saas.service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ScraperService {

    public BigDecimal buscarPreco(String url) {
        // Inicia o Playwright e abre o Chrome invisível (headless = true)
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            Page page = browser.newPage();

            System.out.println("Navegando para: " + url);
            page.navigate(url);

            // O robô procura na tela elementos que costumam ter o preço
            // (Amazon usa .a-price-whole, Mercado Livre usa .andes-money-amount__fraction)
            String seletor = ".a-price-whole, .andes-money-amount__fraction";

            // Espera o preço aparecer na tela por no máximo 10 segundos
            page.waitForSelector(seletor, new Page.WaitForSelectorOptions().setTimeout(10000));

            // Extrai o texto do preço
            String precoTexto = page.innerText(seletor);
            browser.close();

            return formatarPreco(precoTexto);

        } catch (Exception e) {
            System.out.println("❌ Erro ao buscar preço: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    // Método auxiliar para transformar "R$ 3.500,00" no número 3500.00 para o banco de dados
    private BigDecimal formatarPreco(String precoTexto) {
        // Remove tudo que não for número e troca vírgula por ponto
        String limpo = precoTexto.replaceAll("[^0-9]", "");
        return new BigDecimal(limpo);
    }
}