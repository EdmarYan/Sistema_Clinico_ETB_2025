package com.revitafisio.auth.service;

import com.revitafisio.auth.dto.AuthRequest;
import com.revitafisio.auth.dto.AuthResponse;
import com.revitafisio.entities.usuarios.repository.UsuarioRepository;
import com.revitafisio.exception.BusinessRuleException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Serviço responsável pela lógica de negócio da autenticação.
 *
 * Esta classe é o "cérebro" do processo de login. Ela recebe os dados do controller,
 * aplica as regras de validação e interage com a camada de dados (repositório)
 * para verificar as credenciais do usuário.
 *
 * Anotação:
 * @Service: Marca esta classe como um componente de serviço do Spring, tornando-a
 * elegível para injeção de dependência e para conter a lógica de negócio.
 */
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // A injeção de dependência via construtor é a prática recomendada.
    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Autentica um usuário com base nas credenciais fornecidas.
     *
     * @param request O DTO {@link AuthRequest} contendo o CPF e a senha.
     * @return Um {@link AuthResponse} com os dados básicos do usuário se a autenticação for bem-sucedida.
     * @throws BusinessRuleException se as credenciais forem inválidas ou se o usuário estiver inativo.
     */
    public AuthResponse autenticar(AuthRequest request) {

        // 1. Busca o usuário pelo CPF.
        var usuario = usuarioRepository.findByCpf(request.cpf())
                .orElseThrow(() -> new BusinessRuleException("CPF ou senha inválidos."));

        // 2. Validação de Status (Regra de Negócio).
        if (!usuario.isAtivo()) {
            throw new BusinessRuleException("Este usuário está inativo e não pode acessar o sistema.");
        }

        // 3. Validação da Senha (hash BCrypt).
        if (!passwordEncoder.matches(request.senha(), usuario.getSenha())) {
            throw new BusinessRuleException("CPF ou senha inválidos.");
        }

        // 4. Sucesso na Autenticação.
        // Gera um token JWT assinado para representar essa sessão, e monta o
        // objeto de resposta (DTO) com os dados necessários para o frontend.
        String token = jwtService.gerarToken(usuario.getIdUsuario(), usuario.getClass().getSimpleName().toUpperCase());

        return new AuthResponse(
                usuario.getIdUsuario(),
                usuario.getNome(),
                usuario.getClass().getSimpleName().toUpperCase(), // Ex: "FISIOTERAPEUTA", "ADMIN"
                token
        );
    }
}