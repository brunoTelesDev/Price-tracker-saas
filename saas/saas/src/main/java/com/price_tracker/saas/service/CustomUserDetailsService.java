package com.price_tracker.saas.service;

import com.price_tracker.saas.model.Usuario;
import com.price_tracker.saas.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilizador não encontrado: " + email));

        // Pega a role do banco ("ROLE_ADMIN" ou "ROLE_USER") e repassa para o Spring Security
        String userRole = (usuario.getRole() != null && !usuario.getRole().isBlank())
                ? usuario.getRole()
                : "ROLE_USER";

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(userRole);

        return new User(
                usuario.getEmail(),
                usuario.getSenha(),
                List.of(authority) // Injeta a autorização do usuário na sessão
        );
    }
}