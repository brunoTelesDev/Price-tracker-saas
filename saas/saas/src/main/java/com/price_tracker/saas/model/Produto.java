package com.price_tracker.saas.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Entity
@Table(name = "produtos")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String url;

    @Column(columnDefinition = "boolean default true")
    private boolean ativo = true;

    private String nome;

    private BigDecimal precoAtual;

    @Column(nullable = false)
    private BigDecimal precoDesejado;

    // Exclusão em cascata: ao deletar o produto, limpa todo o histórico associado automaticamente
    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistoricoPreco> historicos;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}