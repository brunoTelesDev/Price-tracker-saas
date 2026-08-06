package com.price_tracker.saas.controller;

import com.price_tracker.saas.model.HistoricoPreco;
import com.price_tracker.saas.model.Produto;
import com.price_tracker.saas.model.Usuario;
import com.price_tracker.saas.repository.HistoricoPrecoRepository;
import com.price_tracker.saas.repository.ProdutoRepository;
import com.price_tracker.saas.repository.UsuarioRepository;
import com.price_tracker.saas.service.ScraperService;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ScraperService scraperService; // O nosso robô!
    private final HistoricoPrecoRepository historicoPrecoRepository; // Histórico de preços para o gráfico

    // Injetando as dependências via construtor
    public ProdutoController(ProdutoRepository produtoRepository,
                             UsuarioRepository usuarioRepository,
                             ScraperService scraperService,
                             HistoricoPrecoRepository historicoPrecoRepository) {
        this.produtoRepository = produtoRepository;
        this.usuarioRepository = usuarioRepository;
        this.scraperService = scraperService;
        this.historicoPrecoRepository = historicoPrecoRepository;
    }

    @PostMapping
    public Produto cadastrarProduto(@RequestBody Produto produto, Authentication authentication) {
        String email = authentication.getName();

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado"));

        produto.setUsuario(usuario);
        produto.setAtivo(true);

        try {
            System.out.println("Chamando o robô para ler a URL: " + produto.getUrl());
            BigDecimal precoLido = scraperService.buscarPreco(produto.getUrl());
            produto.setPrecoAtual(precoLido);

            if (produto.getPrecoDesejado() == null) {
                produto.setPrecoDesejado(precoLido);
            }

            // 1. Salva o produto
            Produto produtoSalvo = produtoRepository.save(produto);

            // 2. 📊 REGISTRA O PRIMEIRO PONTO NO GRÁFICO IMEDIATAMENTE!
            if (precoLido != null && precoLido.compareTo(BigDecimal.ZERO) > 0) {
                HistoricoPreco hp = new HistoricoPreco();
                hp.setProduto(produtoSalvo);
                hp.setData(java.time.LocalDate.now());
                hp.setPrecoMinimoDoDia(precoLido);
                historicoPrecoRepository.save(hp);
                System.out.println("📊 Primeiro ponto do histórico registrado no banco!");
            }

            return produtoSalvo;

        } catch (Exception e) {
            System.err.println("Erro ao buscar preço com o robô: " + e.getMessage());
            produto.setPrecoAtual(BigDecimal.ZERO);
            produto.setPrecoDesejado(BigDecimal.ZERO);
            return produtoRepository.save(produto);
        }
    }

    @GetMapping
    public List<Produto> listarProdutos(Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado"));
        return produtoRepository.findByUsuarioId(usuario.getId());
    }

    // Endpoint para retornar o histórico de um produto específico em formato JSON para o gráfico
    @GetMapping("/{id}/historico")
    public List<HistoricoPreco> obterHistoricoDoProduto(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        // Garante que o usuário só pode ver o histórico dos seus próprios produtos
        if (produto.getUsuario().getId().equals(usuario.getId())) {
            return historicoPrecoRepository.findByProdutoIdOrderByDataAsc(id);
        }

        throw new RuntimeException("Acesso negado");
    }

    // 1. Deletar um produto específico pelo ID
    @DeleteMapping("/{id}")
    public void deletarProduto(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        // Garante que o usuário só pode deletar os próprios produtos
        if (produto.getUsuario().getId().equals(usuario.getId())) {
            produtoRepository.delete(produto);
        }
    }

    // 2. Alternar status de monitoramento (Pausar / Retomar)
    @PatchMapping("/{id}/status")
    public Produto alternarStatus(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        if (produto.getUsuario().getId().equals(usuario.getId())) {
            // Se estiver ativo, pausa. Se estiver pausado, ativa.
            boolean statusAtual = produto.isAtivo();
            produto.setAtivo(!statusAtual);
            return produtoRepository.save(produto);
        }

        throw new RuntimeException("Acesso negado");
    }

    // 3. Deletar todos os produtos do utilizador autenticado
    @Transactional
    @DeleteMapping
    public String deletarTodosMeusProdutos(Authentication authentication) {
        String email = authentication.getName();

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado"));

        // Busca todos os produtos desse usuário
        List<Produto> meusProdutos = produtoRepository.findByUsuarioId(usuario.getId());

        // Apaga todos eles de uma vez do banco de dados
        produtoRepository.deleteAll(meusProdutos);

        return "Faxina concluída! Todos os seus produtos foram excluídos do sistema.";
    }
}