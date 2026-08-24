package com.price_tracker.saas.controller;

import com.price_tracker.saas.model.Produto;
import com.price_tracker.saas.model.Usuario;
import com.price_tracker.saas.repository.ProdutoRepository;
import com.price_tracker.saas.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UsuarioRepository usuarioRepository;
    private final ProdutoRepository produtoRepository;

    public AdminController(UsuarioRepository usuarioRepository, ProdutoRepository produtoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.produtoRepository = produtoRepository;
    }

    // 1. LISTAR TODOS OS USUÁRIOS
    @GetMapping("/usuarios")
    public ResponseEntity<List<Map<String, Object>>> listarTodosUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        List<Map<String, Object>> resposta = new ArrayList<>();

        for (Usuario u : usuarios) {
            Map<String, Object> dados = new HashMap<>();
            dados.put("id", u.getId());
            dados.put("email", u.getEmail());
            dados.put("role", u.getRole() != null ? u.getRole() : "ROLE_USER");
            dados.put("telegramConectado", u.getTelegramChatId() != null && !u.getTelegramChatId().isBlank());

            List<Produto> produtosUsuario = produtoRepository.findByUsuarioId(u.getId());
            dados.put("totalProdutos", produtosUsuario.size());

            resposta.add(dados);
        }

        return ResponseEntity.ok(resposta);
    }

    // 2. VER PRODUTOS DE UM USUÁRIO ESPECÍFICO
    @GetMapping("/usuarios/{id}/produtos")
    public ResponseEntity<List<Produto>> obterProdutosDoUsuario(@PathVariable Long id) {
        List<Produto> produtos = produtoRepository.findByUsuarioId(id);
        return ResponseEntity.ok(produtos);
    }

    // 3. EXCLUIR PRODUTO (COMO ADMIN)
    @DeleteMapping("/produtos/{id}")
    public ResponseEntity<Void> excluirProduto(@PathVariable Long id) {
        if (produtoRepository.existsById(id)) {
            produtoRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // 4. SUSPENDER / EXCLUIR USUÁRIO (OPCIONAL)
    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> excluirUsuario(@PathVariable Long id) {
        if (usuarioRepository.existsById(id)) {
            // Apaga os produtos do usuário primeiro para não quebrar a chave estrangeira
            List<Produto> produtos = produtoRepository.findByUsuarioId(id);
            produtoRepository.deleteAll(produtos);

            usuarioRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}