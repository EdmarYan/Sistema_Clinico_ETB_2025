package com.revitafisio.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Serviço responsável pela geração e validação de tokens JWT (JSON Web Token).
 *
 * Um JWT é um token compacto e auto-contido que codifica informações (claims)
 * de forma segura. Ele é composto por três partes separadas por ponto:
 *   HEADER.PAYLOAD.SIGNATURE
 *
 * - HEADER: metadados (algoritmo de assinatura, tipo do token).
 * - PAYLOAD: as claims — dados úteis como id do usuário e tipo.
 * - SIGNATURE: assinatura digital gerada com a chave secreta, que garante
 *   que o token não foi adulterado.
 *
 * Fluxo completo de autenticação JWT:
 * 1. O usuário faz login com CPF + senha.
 * 2. Se as credenciais forem válidas, este serviço gera um JWT assinado (gerarToken).
 * 3. O frontend armazena o token no localStorage e o envia no header
 *    Authorization de cada requisição: "Bearer eyJhbGciOi..."
 * 4. O {@link JwtAuthenticationFilter} intercepta cada requisição, extrai o token
 *    do header e chama este serviço para validar (validarToken).
 * 5. Se o token for válido, o filtro extrai o ID e o tipo do usuário para
 *    criar um contexto de autenticação do Spring Security.
 *
 * Configuração:
 * As propriedades {@code jwt.secret} e {@code jwt.expiration-ms} são lidas
 * do arquivo application.properties via @Value.
 *
 * @see AuthService
 * @see JwtAuthenticationFilter
 */
@Service
public class JwtService {

    private final SecretKey chaveSecreta;
    private final long tempoExpiracaoMs;

    /**
     * Construtor que recebe os valores de configuração e monta a chave HMAC.
     *
     * @param secret       String secreta usada para assinar o token (mínimo 256 bits / 32 caracteres).
     * @param expirationMs Tempo de vida do token em milissegundos (ex: 86400000 = 24 horas).
     */
    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs
    ) {
        // Keys.hmacShaKeyFor exige uma chave com pelo menos 256 bits (32 bytes).
        // Se a string configurada for menor, a aplicação falha rápido na subida,
        // indicando que a configuração precisa ser corrigida.
        this.chaveSecreta = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.tempoExpiracaoMs = expirationMs;
    }

    // ========================================================================
    //  GERAÇÃO DE TOKEN
    // ========================================================================

    /**
     * Gera um token JWT assinado contendo o ID e o tipo do usuário.
     *
     * Claims incluídas no payload:
     * - sub (subject): o ID do usuário convertido para String (padrão JWT).
     * - tipo: o tipo/perfil do usuário (ex: "ADMIN", "FISIOTERAPEUTA").
     * - iat (issued at): timestamp de emissão.
     * - exp (expiration): timestamp de expiração.
     *
     * @param usuarioId    ID do usuário autenticado.
     * @param tipoUsuario  Tipo/perfil do usuário (ex: "ADMIN", "FISIOTERAPEUTA").
     * @return Uma String compacta com o token JWT assinado.
     */
    public String gerarToken(Integer usuarioId, String tipoUsuario) {
        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + tempoExpiracaoMs);

        return Jwts.builder()
                .subject(usuarioId.toString())          // claim padrão "sub"
                .claim("tipo", tipoUsuario)             // claim customizada
                .issuedAt(agora)                        // claim padrão "iat"
                .expiration(expiracao)                   // claim padrão "exp"
                .signWith(chaveSecreta)                  // assina com HMAC-SHA256
                .compact();                              // serializa para a String "header.payload.signature"
    }

    // ========================================================================
    //  VALIDAÇÃO E EXTRAÇÃO DE DADOS DO TOKEN
    // ========================================================================

    /**
     * Valida um token JWT verificando:
     * 1. A assinatura digital (garante que o token não foi adulterado).
     * 2. A data de expiração (garante que o token ainda está dentro da validade).
     *
     * O método {@code Jwts.parser()} faz ambas as verificações automaticamente:
     * - Se a assinatura não bater com a chave secreta, lança {@link JwtException}.
     * - Se o token estiver expirado, lança {@link ExpiredJwtException}.
     *
     * @param token O token JWT recebido do header Authorization (sem o prefixo "Bearer ").
     * @return {@code true} se o token é válido; {@code false} se é inválido ou expirado.
     */
    public boolean validarToken(String token) {
        try {
            // parseSignedClaims faz a verificação completa: assinatura + expiração.
            // Se qualquer coisa estiver errada, ele lança uma exceção.
            extrairTodasClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // JwtException: assinatura inválida, token malformado, expirado, etc.
            // IllegalArgumentException: token nulo ou vazio.
            return false;
        }
    }

    /**
     * Extrai o ID do usuário (claim "sub") de um token JWT válido.
     *
     * A claim "sub" (subject) armazena o ID do usuário como String (padrão JWT).
     * Este método converte de volta para Integer para uso nas queries do JPA.
     *
     * @param token O token JWT já validado.
     * @return O ID do usuário extraído do token.
     */
    public Integer extrairUsuarioId(String token) {
        String subject = extrairTodasClaims(token).getSubject();
        return Integer.parseInt(subject);
    }

    /**
     * Extrai o tipo/perfil do usuário (claim customizada "tipo") de um token JWT válido.
     *
     * Exemplos de valores retornados: "ADMIN", "FISIOTERAPEUTA", "RECEPCIONISTA".
     *
     * @param token O token JWT já validado.
     * @return O tipo do usuário como String.
     */
    public String extrairTipoUsuario(String token) {
        return extrairTodasClaims(token).get("tipo", String.class);
    }

    /**
     * Método interno que parseia o token e retorna todas as claims do payload.
     *
     * Este é o ponto central de validação: o método {@code parseSignedClaims}
     * verifica a assinatura E a expiração automaticamente. Se qualquer coisa
     * estiver errada, ele lança exceção — por isso os métodos públicos acima
     * podem confiar que, se chegaram aqui sem exceção, o token é válido.
     *
     * @param token O token JWT a ser parseado.
     * @return O objeto {@link Claims} contendo todas as claims do payload.
     * @throws JwtException se o token for inválido, expirado ou adulterado.
     */
    private Claims extrairTodasClaims(String token) {
        return Jwts.parser()
                .verifyWith(chaveSecreta)     // configura a chave para verificar a assinatura
                .build()                       // constrói o parser
                .parseSignedClaims(token)      // parseia, verifica assinatura e expiração
                .getPayload();                 // retorna o payload (claims)
    }
}
