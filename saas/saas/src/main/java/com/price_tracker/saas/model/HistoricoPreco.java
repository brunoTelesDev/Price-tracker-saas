package com.price_tracker.saas.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "historico_precos")
public class HistoricoPreco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal precoMinimoDoDia;

    private LocalDate data;

    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    @JsonIgnoreProperties({"usuario", "historicos"}) // 👈 Evita o erro 500 no JSON
    private Produto produto;
}