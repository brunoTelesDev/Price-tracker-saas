package com.price_tracker.saas.controller;

import com.price_tracker.saas.model.Usuario;
import com.price_tracker.saas.repository.UsuarioRepository;
import com.price_tracker.saas.repository.ProdutoRepository;
import org.springframework.security.access.prepost.PreAuthorize;
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

    // Lista TODOS os usuários e o total de produtos de cada um
    @GetMapping("/usuarios")
    public List<Map<String, Object>> listarTodosUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        List<Map<String, Object>> resposta = new ArrayList<>();

        for (Usuario u : usuarios) {
            Map<String, Object> dados = new HashMap<>();
            dados.put("id", u.getId());
            dados.put("email", u.getEmail());
            dados.put("telegramConectado", u.getTelegramChatId() != null);
            dados.put("totalProdutos", produtoRepository.findByUsuarioId(u.getId()).size());
            resposta.add(dados);
        }

        return resposta;
    }
}