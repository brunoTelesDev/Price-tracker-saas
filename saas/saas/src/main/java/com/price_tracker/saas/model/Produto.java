package com.price_tracker.saas.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "produtos")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String url; // O link da loja (Amazon, Mercado Livre, etc.)

    private String nome; // O nome do produto (nosso robô vai preencher isso depois)

    private BigDecimal precoAtual; // O preço que o robô encontrou

    @Column(nullable = false)
    private BigDecimal precoDesejado; // O preço que você quer pagar para ser avisado

    // Relacionamento: Vários produtos pertencem a Um usuário
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}