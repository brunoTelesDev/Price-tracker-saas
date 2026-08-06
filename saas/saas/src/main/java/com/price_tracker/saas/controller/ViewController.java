package com.price_tracker.saas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

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
    public String dashboard() {
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

}