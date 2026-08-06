package com.price_tracker.saas.service;

import com.price_tracker.saas.model.HistoricoPreco;
import com.price_tracker.saas.model.Produto;
import com.price_tracker.saas.repository.HistoricoPrecoRepository;
import com.price_tracker.saas.repository.ProdutoRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AgendadorService {

    private final ProdutoRepository produtoRepository;
    private final ScraperService scraperService;
    private final TelegramBotService telegramBotService; // O nosso Carteiro!
    private final HistoricoPrecoRepository historicoPrecoRepository; // Repositório para o histórico do gráfico

    // Injetando as dependências
    public AgendadorService(ProdutoRepository produtoRepository,
                            ScraperService scraperService,
                            TelegramBotService telegramBotService,
                            HistoricoPrecoRepository historicoPrecoRepository) {
        this.produtoRepository = produtoRepository;
        this.scraperService = scraperService;
        this.telegramBotService = telegramBotService;
        this.historicoPrecoRepository = historicoPrecoRepository;
    }

    // 🕒 Roda a cada 5 minutos (300000 ms)
    @Scheduled(fixedRate = 300000)
    public void verificarPrecosDosProdutos() {
        System.out.println("\n⏰ [DESPERTADOR] Iniciando verificação automática de preços...");

        // 🛠️ CORREÇÃO: Busca apenas produtos que estão com ativo = true
        List<Produto> produtosAtivos = produtoRepository.findByAtivoTrue();

        if (produtosAtivos.isEmpty()) {
            System.out.println("ℹ️ Nenhum produto ativo cadastrado para verificar.");
            return;
        }

        for (Produto produto : produtosAtivos) {
            // Trava de segurança adicional
            if (!produto.isAtivo()) {
                continue;
            }

            System.out.println("🔎 Verificando: " + produto.getNome());

            try {
                BigDecimal novoPreco = scraperService.buscarPreco(produto.getUrl());

                if (novoPreco != null && novoPreco.compareTo(BigDecimal.ZERO) > 0) {
                    // 1. Atualiza o preço atual do produto
                    produto.setPrecoAtual(novoPreco);
                    produtoRepository.save(produto);

                    // 2. Registra / Atualiza o histórico do dia (Menor preço do dia)
                    salvarOuAtualizarHistoricoDoDia(produto, novoPreco);

                    // 3. 🚨 REGRA DO ALERTA: O preço atual bateu a meta?
                    if (novoPreco.compareTo(produto.getPrecoDesejado()) <= 0) {
                        System.out.println("🚨 [ALERTA] Meta atingida para: " + produto.getNome());

                        String seuChatId = "6878602610";
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
            } catch (Exception e) {
                System.err.println("❌ Erro ao verificar o produto " + produto.getNome() + ": " + e.getMessage());
            }
        }
        System.out.println("✅ [DESPERTADOR] Verificação concluída!\n");
    }

    /**
     * Regra do Histórico Diário:
     * Garante que apenas 1 registro exista por dia para cada produto.
     * Se já existir, só atualiza se o preço novo for MENOR que o antigo do dia!
     */
    private void salvarOuAtualizarHistoricoDoDia(Produto produto, BigDecimal precoLido) {
        LocalDate hoje = LocalDate.now();

        Optional<HistoricoPreco> historicoHojeOpt = historicoPrecoRepository.findByProdutoIdAndData(produto.getId(), hoje);

        if (historicoHojeOpt.isPresent()) {
            HistoricoPreco historicoHoje = historicoHojeOpt.get();
            // Se o novo preço for menor do que o que já estava salvo hoje, atualiza!
            if (precoLido.compareTo(historicoHoje.getPrecoMinimoDoDia()) < 0) {
                historicoHoje.setPrecoMinimoDoDia(precoLido);
                historicoPrecoRepository.save(historicoHoje);
                System.out.println("📊 Novo preço mínimo do dia registrado no histórico para: " + produto.getNome());
            }
        } else {
            // Primeiro registro do dia
            HistoricoPreco novoHistorico = new HistoricoPreco();
            novoHistorico.setProduto(produto);
            novoHistorico.setData(hoje);
            novoHistorico.setPrecoMinimoDoDia(precoLido);
            historicoPrecoRepository.save(novoHistorico);
            System.out.println("📊 Primeiro preço do dia registrado no histórico para: " + produto.getNome());
        }
    }
}