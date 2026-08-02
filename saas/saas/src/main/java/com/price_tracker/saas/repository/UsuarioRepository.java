package com.price_tracker.saas.repository;

import com.price_tracker.saas.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // O Spring é tão inteligente que só de escrevermos o nome do método em inglês,
    // ele escreve o comando SQL de busca sozinho!
    Optional<Usuario> findByEmail(String email);
}