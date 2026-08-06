package com.price_tracker.saas.service;

import com.price_tracker.saas.model.Produto;
import com.price_tracker.saas.repository.ProdutoRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AgendadorService {

    private final ProdutoRepository produtoRepository;
    private final ScraperService scraperService;
    private final TelegramBotService telegramBotService; // O nosso Carteiro!

    // Injetando as 3 ferramentas
    public AgendadorService(ProdutoRepository produtoRepository, ScraperService scraperService, TelegramBotService telegramBotService) {
        this.produtoRepository = produtoRepository;
        this.scraperService = scraperService;
        this.telegramBotService = telegramBotService;
    }

    // 🕒 Roda a cada 5 minutos (300000 ms)
    @Scheduled(fixedRate = 300000)
    public void verificarPrecosDosProdutos() {
        System.out.println("\n⏰ [DESPERTADOR] Iniciando verificação automática de preços...");

        List<Produto> produtos = produtoRepository.findAll();

        if (produtos.isEmpty()) {
            System.out.println("ℹ️ Nenhum produto cadastrado para verificar.");
            return;
        }

        for (Produto produto : produtos) {
            System.out.println("🔎 Verificando: " + produto.getNome());

            BigDecimal novoPreco = scraperService.buscarPreco(produto.getUrl());

            if (novoPreco.compareTo(BigDecimal.ZERO) > 0) {
                produto.setPrecoAtual(novoPreco);
                produtoRepository.save(produto);

                // 🚨 REGRA DO ALERTA: O preço atual bateu a meta?
                if (novoPreco.compareTo(produto.getPrecoDesejado()) <= 0) {
                    System.out.println("🚨 [ALERTA] Meta atingida para: " + produto.getNome());

                    // 📱 MANDANDO PARA O SEU CELULAR!
                    String seuChatId = "6878602610"; // O seu ID que você acabou de me mandar
                    telegramBotService.enviarAlerta(
                            seuChatId,
                            produto.getNome(),
                            produto.getUrl(),
                            novoPreco.toString()
                    );

                } else {
                    System.out.println("⏳ O produto " + produto.getNome() + " ainda está caro.");
                }
            }
        }
        System.out.println("✅ [DESPERTADOR] Verificação concluída!\n");
    }
}