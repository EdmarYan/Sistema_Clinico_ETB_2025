package com.revitafisio.auth.service;

import com.revitafisio.auth.dto.AuthRequest;
import com.revitafisio.auth.dto.AuthResponse;
import com.revitafisio.entities.usuarios.Usuario;
import com.revitafisio.entities.usuarios.repository.UsuarioRepository;
import com.revitafisio.exception.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes Unitários para a classe AuthService usando Mockito.
 *
 * O que é Mockito?
 * O AuthService não trabalha sozinho: ele precisa buscar no Banco de Dados (UsuarioRepository)
 * e conferir a senha (PasswordEncoder). Mas em um teste unitário, não queremos conectar ao
 * banco de dados real. Queremos apenas testar a LÓGICA do AuthService.
 *
 * É aí que entra o Mockito! Ele cria objetos falsos ("Mocks") que fingem ser o repositório.
 * Nós programamos as respostas desses Mocks para testar diferentes cenários.
 */
@ExtendWith(MockitoExtension.class) // Habilita o uso das anotações do Mockito (@Mock, @InjectMocks) no JUnit 5
class AuthServiceTest {

    // Cria uma versão "de mentirinha" do repositório
    @Mock
    private UsuarioRepository usuarioRepository;

    // Cria uma versão de mentirinha do validador de senhas
    @Mock
    private PasswordEncoder passwordEncoder;

    // Cria uma versão de mentirinha do gerador de token
    @Mock
    private JwtService jwtService;

    // Cria a classe real que vamos testar e, automaticamente, injeta os Mocks acima dentro dela.
    @InjectMocks
    private AuthService authService;

    private AuthRequest authRequestValido;

    // Criamos uma classe anônima rápida que herda de Usuario para o teste, já que Usuario é abstract.
    private Usuario usuarioFicticio;

    @BeforeEach
    void setUp() {
        authRequestValido = new AuthRequest("12345678900", "senha123");

        usuarioFicticio = new Usuario() {
            @Override
            public String getNome() { return "João Silva"; }
        };
        usuarioFicticio.setIdUsuario(1);
        usuarioFicticio.setCpf("12345678900");
        usuarioFicticio.setSenha("senha_hash_banco");
        usuarioFicticio.setAtivo(true);
    }

    @Test
    @DisplayName("Deve autenticar com sucesso e retornar o Token quando credenciais forem corretas")
    void deveAutenticarComSucesso() {
        // 1. Arrange: Ensinamos os Mocks como eles devem se comportar.
        // Quando o repositório for chamado buscando esse CPF, devolva nosso usuário fictício.
        when(usuarioRepository.findByCpf("12345678900")).thenReturn(Optional.of(usuarioFicticio));
        
        // Quando o passwordEncoder comparar a senha da requisição com a senha do banco, responda "true" (senha bate)
        when(passwordEncoder.matches("senha123", "senha_hash_banco")).thenReturn(true);
        
        // Quando o jwtService for chamado para gerar um token, devolva um texto fake
        when(jwtService.gerarToken(eq(1), anyString())).thenReturn("token.jwt.fake");

        // 2. Act: Rodamos o método de verdade
        AuthResponse resposta = authService.autenticar(authRequestValido);

        // 3. Assert: Conferimos o resultado
        assertNotNull(resposta);
        assertEquals("João Silva", resposta.nome());
        assertEquals("token.jwt.fake", resposta.token());

        // Garantimos que os mocks foram chamados
        verify(usuarioRepository, times(1)).findByCpf("12345678900");
        verify(passwordEncoder, times(1)).matches("senha123", "senha_hash_banco");
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException quando o usuário não for encontrado no banco")
    void deveFalharQuandoCpfNaoExistir() {
        // Arrange: Dizemos que o repositório vai retornar vazio (usuário não existe)
        when(usuarioRepository.findByCpf("12345678900")).thenReturn(Optional.empty());

        // Act & Assert: Tentar autenticar deve "estourar" uma exceção de Regra de Negócio
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class, 
                () -> authService.autenticar(authRequestValido)
        );

        assertEquals("CPF ou senha inválidos.", exception.getMessage());
        
        // Como falhou logo no início, o passwordEncoder NUNCA deve ter sido chamado. (Verificação extra)
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException quando o usuário estiver inativo")
    void deveFalharQuandoUsuarioInativo() {
        // Arrange
        usuarioFicticio.setAtivo(false); // Mudamos o estado do mock
        when(usuarioRepository.findByCpf("12345678900")).thenReturn(Optional.of(usuarioFicticio));

        // Act & Assert
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class, 
                () -> authService.autenticar(authRequestValido)
        );

        assertEquals("Este usuário está inativo e não pode acessar o sistema.", exception.getMessage());
    }
}
