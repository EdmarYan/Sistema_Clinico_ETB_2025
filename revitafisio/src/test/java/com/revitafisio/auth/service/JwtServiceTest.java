package com.revitafisio.auth.service;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes Unitários para a classe JwtService.
 *
 * O que é um Teste Unitário?
 * É um teste que verifica o comportamento de uma única "unidade" de código,
 * geralmente uma classe isolada. Aqui, NÃO carregamos o Spring Boot (não usamos @SpringBootTest),
 * o que faz o teste rodar extremamente rápido.
 *
 * Por que testar o JwtService isoladamente?
 * Porque a geração e validação de tokens envolve criptografia e tempo de expiração.
 * É crucial garantir que:
 * 1. O token é gerado com os dados certos.
 * 2. O token gerado pode ser lido e validado corretamente.
 * 3. Um token modificado ou inventado será rejeitado.
 */
class JwtServiceTest {

    // A instância da classe que vamos testar. Chamamos carinhosamente de "sut" (System Under Test).
    private JwtService jwtService;

    /**
     * O método anotado com @BeforeEach roda ANTES de CADA teste.
     * Serve para preparar o cenário, garantindo que todo teste comece com um estado limpo.
     */
    @BeforeEach
    void setUp() {
        // Inicializamos o serviço passando a chave secreta e o tempo de expiração (1 hora).
        // Como o Spring injetaria esses valores pelo application.properties via @Value,
        // em um teste unitário nós mesmos fornecemos os valores manualmente no construtor.
        jwtService = new JwtService(
            "MinhaChaveSuperSecretaDeTesteDeDesenvolvimentoParaGarantir32Caracteres", 
            3600000L
        );
    }

    @Test
    @DisplayName("Deve gerar um token válido contendo o ID e a Role do usuário")
    void deveGerarTokenComSucesso() {
        // 1. Arrange (Preparação)
        Integer idUsuario = 15;
        String role = "ADMIN";

        // 2. Act (Ação)
        String token = jwtService.gerarToken(idUsuario, role);

        // 3. Assert (Verificação)
        assertNotNull(token, "O token não deveria ser nulo.");
        assertFalse(token.isBlank(), "O token não deveria ser vazio.");
        
        // Verifica se conseguimos ler as informações de volta corretamente
        assertTrue(jwtService.validarToken(token), "O token gerado deveria ser válido.");
        assertEquals(idUsuario, jwtService.extrairUsuarioId(token), "O ID extraído deveria ser igual ao ID fornecido.");
        assertEquals(role, jwtService.extrairTipoUsuario(token), "A Role extraída deveria ser igual à Role fornecida.");
    }

    @Test
    @DisplayName("Deve retornar false quando tentar validar um token inválido (falso ou modificado)")
    void deveRejeitarTokenInvalido() {
        // 1. Arrange: Inventamos um token que não foi assinado pelo nosso sistema
        String tokenFalso = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwi" +
                "aWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

        // 2. Act
        boolean isValido = jwtService.validarToken(tokenFalso);

        // 3. Assert
        assertFalse(isValido, "O token falso não deveria ser considerado válido.");
    }
}
