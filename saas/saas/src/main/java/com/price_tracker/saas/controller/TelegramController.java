package com.price_tracker.saas.controller;

import com.price_tracker.saas.model.Usuario;
import com.price_tracker.saas.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/telegram")
public class TelegramController {

    private final UsuarioRepository usuarioRepository;

    @Value("${telegram.bot.username:MeuPriceTracker_bot}")
    private String botUsername;

    public TelegramController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // =========================================================
    // GERAR CÓDIGO DE CONEXÃO
    // =========================================================

    @PostMapping("/conectar")
    public Map<String, String> gerarCodigo(Authentication authentication) {

        String email = authentication.getName();

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Gera código único de 6 caracteres maiúsculos
        String codigo = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 6)
                .toUpperCase();

        usuario.setTelegramCodigo(codigo);
        usuarioRepository.save(usuario);

// Você pode retornar o protocolo do app que preserva o parâmetro:
        String linkTelegram = "tg://resolve?domain=" + botUsername + "&start=" + codigo;

        System.out.println("🔐 Código Telegram gerado para " + usuario.getEmail() + ": " + codigo);
        System.out.println("🔗 Link Telegram: " + linkTelegram);

        Map<String, String> resposta = new HashMap<>();
        resposta.put("codigo", codigo);
        resposta.put("link", linkTelegram);

        return resposta;
    }

    // =========================================================
    // VERIFICAR STATUS DA CONEXÃO
    // =========================================================

    @GetMapping("/status")
    public boolean verificarStatus(Authentication authentication) {

        String email = authentication.getName();

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return usuario.getTelegramChatId() != null;
    }
}