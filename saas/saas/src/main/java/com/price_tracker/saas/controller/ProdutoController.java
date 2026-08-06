package com.price_tracker.saas.controller;

import com.price_tracker.saas.model.Produto;
import com.price_tracker.saas.model.Usuario;
import com.price_tracker.saas.repository.ProdutoRepository;
import com.price_tracker.saas.repository.UsuarioRepository;
import com.price_tracker.saas.service.ScraperService;
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

    // Injetando as dependências
    public ProdutoController(ProdutoRepository produtoRepository, UsuarioRepository usuarioRepository, ScraperService scraperService) {
        this.produtoRepository = produtoRepository;
        this.usuarioRepository = usuarioRepository;
        this.scraperService = scraperService;
    }

    @PostMapping
    public Produto cadastrarProduto(@RequestBody Produto produto, Authentication authentication) {
        String email = authentication.getName();

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado"));

        produto.setUsuario(usuario);

        // 🤖 A MÁGICA ACONTECE AQUI: O robô entra em ação!
        System.out.println("Chamando o robô para ler a URL...");
        BigDecimal precoLido = scraperService.buscarPreco(produto.getUrl());
        produto.setPrecoAtual(precoLido);

        return produtoRepository.save(produto);
    }

    @GetMapping
    public List<Produto> listarProdutos(Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado"));
        return produtoRepository.findByUsuarioId(usuario.getId());
    }
    // 3. Deletar todos os produtos do utilizador autenticado
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