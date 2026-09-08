package com.revitafisio.auth.service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro de autenticação JWT — intercepta TODAS as requisições HTTP antes de
 * chegarem aos controllers.
 *
 * Como funciona o fluxo completo:
 * ┌──────────┐      ┌───────────────────────┐      ┌──────────────────┐      ┌────────────┐
 * │  Client  │ ---> │ JwtAuthenticationFilter│ ---> │ SecurityFilterChain│ ---> │ Controller │
 * │ (Browser)│      │ (este filtro)          │      │ (autoriza/nega)   │      │            │
 * └──────────┘      └───────────────────────┘      └──────────────────┘      └────────────┘
 *
 * 1. O browser envia a requisição com o header: Authorization: Bearer <token>
 * 2. Este filtro extrai o token do header.
 * 3. Chama JwtService.validarToken() para verificar assinatura e expiração.
 * 4. Se válido, extrai o ID e o tipo do usuário das claims do token.
 * 5. Cria um objeto de autenticação do Spring Security e coloca no
 *    SecurityContextHolder — isso diz ao Spring: "este usuário está autenticado".
 * 6. O SecurityFilterChain (configurado em SecurityConfig) decide se a rota
 *    exige autenticação. Se sim e não há autenticação no contexto, retorna 401.
 *
 * Anotações:
 * @Component: Registra este filtro como um bean do Spring, permitindo que o
 *             SecurityConfig o injete na cadeia de filtros.
 *
 * Herança:
 * OncePerRequestFilter: Garante que o filtro é executado exatamente UMA VEZ
 * por requisição (mesmo em casos de forward/redirect internos do servlet).
 *
 * @see JwtService
 * @see com.revitafisio.config.SecurityConfig
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    /**
     * Injeção de dependência via construtor.
     * O JwtService é quem sabe validar o token e extrair as informações dele.
     */
    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Método principal do filtro — executado para CADA requisição HTTP.
     *
     * @param request     A requisição HTTP recebida.
     * @param response    A resposta HTTP que será enviada.
     * @param filterChain A cadeia de filtros do Spring Security. Chamar
     *                    filterChain.doFilter() passa a requisição para o próximo
     *                    filtro da cadeia (ou para o controller, se for o último).
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // --- PASSO 1: Extrair o token do header Authorization ---
        // O padrão HTTP para tokens é: Authorization: Bearer eyJhbGciOi...
        // "Bearer " tem 7 caracteres — o token começa no índice 7.
        String authHeader = request.getHeader("Authorization");
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // Remove o prefixo "Bearer "
        }

        // --- PASSO 2: Validar o token (se presente) ---
        // Se não há token, simplesmente deixamos a requisição seguir.
        // O SecurityFilterChain decidirá se a rota precisa de autenticação.
        // Se precisar e não tiver, retorna 401 automaticamente.
        if (token != null && jwtService.validarToken(token)) {

            // --- PASSO 3: Extrair informações do usuário das claims ---
            Integer usuarioId = jwtService.extrairUsuarioId(token);
            String tipoUsuario = jwtService.extrairTipoUsuario(token);

            // --- PASSO 4: Criar o objeto de autenticação do Spring Security ---
            // UsernamePasswordAuthenticationToken é a implementação padrão de
            // Authentication no Spring Security. Ele recebe:
            //   - principal: a identidade do usuário (usamos o ID).
            //   - credentials: a senha (não precisamos, já validamos via token).
            //   - authorities: as permissões/roles do usuário.
            //
            // SimpleGrantedAuthority("ROLE_ADMIN") permite usar @PreAuthorize
            // ou .hasRole("ADMIN") no futuro para controle de acesso por perfil.
            var autenticacao = new UsernamePasswordAuthenticationToken(
                    usuarioId,                                           // principal (quem é o usuário)
                    null,                                                // credentials (não necessário com JWT)
                    List.of(new SimpleGrantedAuthority("ROLE_" + tipoUsuario)) // authorities (ex: ROLE_ADMIN)
            );

            // --- PASSO 5: Registrar a autenticação no contexto do Spring ---
            // O SecurityContextHolder é o "cofre" onde o Spring guarda a
            // informação de quem está autenticado na requisição atual.
            // Ao setar aqui, todos os filtros e controllers subsequentes
            // conseguem acessar o usuário autenticado.
            SecurityContextHolder.getContext().setAuthentication(autenticacao);
        }

        // --- PASSO 6: Passar a requisição adiante na cadeia de filtros ---
        // IMPORTANTE: este método SEMPRE deve ser chamado, mesmo se o token for
        // inválido ou ausente. A decisão de bloquear (401) ou permitir é
        // responsabilidade do SecurityFilterChain, não deste filtro.
        filterChain.doFilter(request, response);
    }
}
