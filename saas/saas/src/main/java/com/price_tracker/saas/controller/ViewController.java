package com.price_tracker.saas.controller;

import com.price_tracker.saas.model.Usuario;
import com.price_tracker.saas.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    private final UsuarioRepository usuarioRepository;

    public ViewController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/cadastro")
    public String cadastro() {
        return "cadastro";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();

            // Pega o nome a partir da primeira parte do e-mail (ex: bruno em bruno@email.com)
            String nomeExibicao = email.contains("@") ? email.split("@")[0] : email;

            model.addAttribute("nomeUsuario", nomeExibicao);
        }

        return "dashboard";
    }

    @GetMapping("/produtos")
    public String produtos() {
        return "produtos";
    }

    @GetMapping("/historico")
    public String historico() {
        return "historico";
    }

    @GetMapping("/perfil")
    public String perfil() {
        return "perfil";
    }
    @GetMapping("/admin")
    public String admin() {
        return "admin"; // Retorna admin.html
    }
}