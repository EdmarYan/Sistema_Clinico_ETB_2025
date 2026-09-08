package com.revitafisio.config;

import com.revitafisio.auth.service.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuração central de segurança da aplicação.
 *
 * Esta classe faz duas coisas:
 * 1. Fornece o {@link PasswordEncoder} (BCrypt) para hash de senhas.
 * 2. Define o {@link SecurityFilterChain} — o pipeline de segurança HTTP que
 *    decide quais rotas são públicas e quais exigem um token JWT válido.
 *
 * Conceitos importantes para entender esta configuração:
 *
 * - CSRF (Cross-Site Request Forgery): Proteção contra ataques onde um site
 *   malicioso envia requisições em nome do usuário. Como usamos JWT (stateless)
 *   em vez de cookies de sessão, CSRF não se aplica e é desabilitado.
 *
 * - Sessão STATELESS: O servidor NÃO guarda estado de sessão. Cada requisição
 *   deve trazer o token JWT no header Authorization. Isso é o padrão para APIs REST.
 *
 * - Cadeia de Filtros: O Spring Security processa cada requisição através de uma
 *   cadeia (chain) de filtros. Nosso JwtAuthenticationFilter é inserido ANTES do
 *   filtro padrão do Spring para interceptar o token e autenticar o usuário.
 *
 * Anotações:
 * @Configuration: Marca esta classe como fonte de beans de configuração do Spring.
 * @EnableWebSecurity: Ativa o módulo de segurança web do Spring Security,
 *                     habilitando a proteção HTTP e o controle de acesso por rota.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Injeção de dependência via construtor.
     * O JwtAuthenticationFilter é o filtro que criamos para validar tokens JWT.
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * BCrypt é o algoritmo padrão recomendado pelo Spring para hash de senha:
     * é lento de propósito (dificulta ataque de força bruta) e gera um "salt"
     * aleatório embutido no próprio hash, então duas senhas iguais nunca geram
     * o mesmo hash.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Define o pipeline de segurança HTTP da aplicação.
     *
     * Este bean é o coração da configuração: ele diz ao Spring Security
     * QUAIS rotas são públicas (não precisam de token) e QUAIS são protegidas
     * (exigem um JWT válido no header Authorization).
     *
     * Ordem de avaliação das regras:
     * O Spring Security avalia as regras de cima para baixo. A PRIMEIRA regra
     * que combinar com a URL da requisição é a que vale. Por isso, as rotas
     * públicas (permitAll) devem vir ANTES da regra genérica (anyRequest).
     *
     * @param http O objeto HttpSecurity que permite configurar a segurança.
     * @return O SecurityFilterChain construído e pronto para uso.
     * @throws Exception em caso de erro na configuração.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // --- 1. Desabilitar CSRF ---
            // CSRF é relevante para aplicações baseadas em sessão/cookie.
            // Como usamos JWT (token no header), não precisamos de proteção CSRF.
            .csrf(csrf -> csrf.disable())

            // --- 2. Configurar política de sessão ---
            // STATELESS = o Spring Security NÃO cria nem usa HttpSession.
            // Cada requisição é independente e deve trazer seu próprio token JWT.
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // --- 3. Definir regras de acesso por rota ---
            .authorizeHttpRequests(auth -> auth

                // ---- ROTAS PÚBLICAS (sem token) ----

                // Endpoint de login — o usuário ainda não tem token, precisa ser público.
                .requestMatchers("/auth/login").permitAll()

                // Landing page e páginas institucionais (site público).
                .requestMatchers(
                    "/",                    // Página inicial (index.html)
                    "/index.html",
                    "/login.html",
                    "/sobre.html",
                    "/consultas.html",
                    "/contato.html",
                    "/header.html"
                ).permitAll()

                // Arquivos estáticos do frontend (CSS, JS, imagens, fontes, vendor).
                // Sem isso, o browser não consegue carregar a interface.
                .requestMatchers(
                    "/css/**",
                    "/js/**",
                    "/img/**",
                    "/vendor/**",
                    "/landingpage/**",
                    "/pages/**",             // Todas as páginas, JS, CSS dentro de pages
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()

                // ---- ROTAS PROTEGIDAS (exigem token JWT válido) ----
                // Qualquer outra rota que não se encaixe nas regras acima
                // (ou seja, todos os endpoints da API REST) exige autenticação.
                .anyRequest().authenticated()
            )

            // --- 4. Tratamento de Exceções de Segurança (401/403) ---
            // Retorna um JSON para o frontend em vez de uma página HTML de erro.
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json");
                    response.setStatus(401);
                    response.getWriter().write("{\"message\": \"Erro 401: Acesso Negado ou Token Inválido.\"}");
                })
            )

            // --- 5. Inserir o filtro JWT na cadeia de filtros ---
            // addFilterBefore = nosso filtro roda ANTES do filtro padrão do Spring.
            // Assim, quando o Spring for verificar se o usuário está autenticado,
            // nosso filtro já terá validado o token e setado a autenticação no contexto.
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}