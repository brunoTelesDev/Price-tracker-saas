package com.price_tracker.saas.repository;

import com.price_tracker.saas.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    // O Spring cria o SQL automático para buscar todos os produtos de um usuário específico!
    List<Produto> findByUsuarioId(Long usuarioId);
}