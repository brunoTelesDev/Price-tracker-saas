package com.price_tracker.saas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

                        // 👑 2. RESTRIÇÃO SUPREMA: Apenas usuários com ROLE_ADMIN acessam rotas de Admin
                        .requestMatchers("/admin", "/admin/**", "/api/admin/**").hasRole("ADMIN")

                        // 3. Permite acesso explícito às páginas do sistema para usuários logados
                        .requestMatchers("/dashboard", "/historico", "/produtos", "/perfil").authenticated()

                        // 4. Garante que os métodos REST da API funcionem para usuários logados
                        .requestMatchers("/api/produtos/**", "/api/telegram/**").authenticated()

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