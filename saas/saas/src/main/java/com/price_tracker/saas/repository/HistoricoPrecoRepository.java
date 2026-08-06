package com.price_tracker.saas.repository;

import com.price_tracker.saas.model.HistoricoPreco;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HistoricoPrecoRepository extends JpaRepository<HistoricoPreco, Long> {

    // Busca o histórico do dia de um produto específico
    Optional<HistoricoPreco> findByProdutoIdAndData(Long produtoId, LocalDate data);

    // Busca todo o histórico de um produto ordenado por data
    List<HistoricoPreco> findByProdutoIdOrderByDataAsc(Long produtoId);
}