package com.price_tracker.saas.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String role = "ROLE_USER"; // Valor padrão para novos cadastros

    @Column(name = "telegram_chat_id")
    private String telegramChatId;

    @Column(name = "telegram_codigo")
    private String telegramCodigo;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;
}