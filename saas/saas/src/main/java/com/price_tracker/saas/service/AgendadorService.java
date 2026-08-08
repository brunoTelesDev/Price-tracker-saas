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
    private final TelegramBotService telegramBotService;
    private final HistoricoPrecoRepository historicoPrecoRepository;

    public AgendadorService(
            ProdutoRepository produtoRepository,
            ScraperService scraperService,
            TelegramBotService telegramBotService,
            HistoricoPrecoRepository historicoPrecoRepository
    ) {
        this.produtoRepository = produtoRepository;
        this.scraperService = scraperService;
        this.telegramBotService = telegramBotService;
        this.historicoPrecoRepository = historicoPrecoRepository;
    }

    // Roda a cada 5 minutos
    @Scheduled(fixedRate = 300000)
    public void verificarPrecosDosProdutos() {

        System.out.println("\n⏰ [DESPERTADOR] Iniciando verificação automática de preços...");

        List<Produto> produtosAtivos = produtoRepository.findByAtivoTrue();

        if (produtosAtivos.isEmpty()) {
            System.out.println("ℹ️ Nenhum produto ativo cadastrado para verificar.");
            return;
        }

        for (Produto produto : produtosAtivos) {

            if (!produto.isAtivo()) {
                continue;
            }

            System.out.println("🔎 Verificando: " + produto.getNome());

            try {

                BigDecimal novoPreco = scraperService.buscarPreco(produto.getUrl());

                if (novoPreco != null && novoPreco.compareTo(BigDecimal.ZERO) > 0) {

                    // Atualiza preço atual
                    produto.setPrecoAtual(novoPreco);
                    produtoRepository.save(produto);

                    // Atualiza histórico
                    salvarOuAtualizarHistoricoDoDia(produto, novoPreco);

                    // Verifica se atingiu a meta
                    if (produto.getPrecoDesejado() != null
                            && novoPreco.compareTo(produto.getPrecoDesejado()) <= 0) {

                        System.out.println(
                                "🚨 [ALERTA] Meta atingida para: "
                                        + produto.getNome()
                        );

                        // ==========================================
                        // PEGA O DONO DO PRODUTO
                        // ==========================================

                        String chatId = produto.getUsuario().getTelegramChatId();

                        // ==========================================
                        // VERIFICA SE O USUÁRIO TEM TELEGRAM
                        // ==========================================

                        if (chatId != null && !chatId.isBlank()) {

                            System.out.println(
                                    "📨 Enviando alerta para o usuário: "
                                            + produto.getUsuario().getEmail()
                                            + " | Chat ID: "
                                            + chatId
                            );

                            telegramBotService.enviarAlerta(
                                    chatId,
                                    produto.getNome(),
                                    produto.getUrl(),
                                    novoPreco.toString()
                            );

                        } else {

                            System.out.println(
                                    "⚠️ Usuário "
                                            + produto.getUsuario().getEmail()
                                            + " ainda não possui Telegram vinculado."
                            );
                        }

                    } else {

                        System.out.println(
                                "⏳ O produto "
                                        + produto.getNome()
                                        + " ainda está caro."
                        );
                    }
                }

            } catch (Exception e) {

                System.err.println(
                        "❌ Erro ao verificar o produto "
                                + produto.getNome()
                                + ": "
                                + e.getMessage()
                );
            }
        }

        System.out.println("✅ [DESPERTADOR] Verificação concluída!\n");
    }

    /**
     * Regra do Histórico Diário:
     *
     * Mantém apenas um registro por dia para cada produto.
     * Se o preço novo for menor, atualiza o mínimo do dia.
     */
    private void salvarOuAtualizarHistoricoDoDia(
            Produto produto,
            BigDecimal precoLido
    ) {

        LocalDate hoje = LocalDate.now();

        Optional<HistoricoPreco> historicoHojeOpt =
                historicoPrecoRepository.findByProdutoIdAndData(
                        produto.getId(),
                        hoje
                );

        if (historicoHojeOpt.isPresent()) {

            HistoricoPreco historicoHoje =
                    historicoHojeOpt.get();

            if (precoLido.compareTo(
                    historicoHoje.getPrecoMinimoDoDia()
            ) < 0) {

                historicoHoje.setPrecoMinimoDoDia(precoLido);

                historicoPrecoRepository.save(historicoHoje);

                System.out.println(
                        "📊 Novo preço mínimo do dia registrado para: "
                                + produto.getNome()
                );
            }

        } else {

            HistoricoPreco novoHistorico =
                    new HistoricoPreco();

            novoHistorico.setProduto(produto);
            novoHistorico.setData(hoje);
            novoHistorico.setPrecoMinimoDoDia(precoLido);

            historicoPrecoRepository.save(novoHistorico);

            System.out.println(
                    "📊 Primeiro preço do dia registrado para: "
                            + produto.getNome()
            );
        }
    }
}