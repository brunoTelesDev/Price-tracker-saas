package com.price_tracker.saas.controller;

import com.price_tracker.saas.model.Usuario;
import com.price_tracker.saas.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/cadastrar")
    public String cadastrar(@RequestBody Usuario usuario) {
        // Encripta a senha com BCrypt antes de guardar na base de dados!
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        usuarioRepository.save(usuario);
        return "Utilizador " + usuario.getEmail() + " registado com sucesso com senha encriptada!";
    }
}