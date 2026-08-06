package com.price_tracker.saas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Desabilita CSRF para permitir requisições fetch/REST do JavaScript
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // 1. Recursos públicos e arquivos estáticos (CSS, JS, Imagens)
                        .requestMatchers(
                                "/login",
                                "/cadastro",
                                "/api/usuarios/cadastrar",
                                "/status",
                                "/css/**",
                                "/js/**",
                                "/img/**",
                                "/webjars/**"
                        ).permitAll()

                        // 2. Permite acesso explícito às páginas de Dashboard e Histórico para usuários logados
                        .requestMatchers("/dashboard", "/historico", "/produtos").authenticated()

                        // 3. Garante que os métodos REST (GET, POST, PATCH, DELETE) na API de produtos funcionem
                        .requestMatchers("/api/produtos/**").authenticated()

                        // Qualquer outra rota exige autenticação
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )

                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}