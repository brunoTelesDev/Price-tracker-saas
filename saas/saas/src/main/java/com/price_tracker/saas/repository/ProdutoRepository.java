package com.price_tracker.saas.repository;

import com.price_tracker.saas.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByUsuarioId(Long usuarioId);

    // 🛠️ NOVO MÉTODO: Busca apenas produtos que estão com ativo = true
    List<Produto> findByAtivoTrue();
}