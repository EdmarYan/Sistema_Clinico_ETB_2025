package com.revitafisio.auth.service;

import com.revitafisio.auth.dto.AuthRequest;
import com.revitafisio.auth.dto.AuthResponse;
import com.revitafisio.entities.usuarios.repository.UsuarioRepository;
import com.revitafisio.exception.BusinessRuleException;
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

    // A injeção de dependência via construtor é a prática recomendada.
    public AuthService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
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
        // O uso do .orElseThrow() com uma exceção específica é uma forma limpa de lidar com
        // o caso em que o usuário não é encontrado.
        // Vínculo: Este metodo 'findByCpf' foi definido na interface UsuarioRepository.
        var usuario = usuarioRepository.findByCpf(request.cpf())
                // CORREÇÃO: Usando BusinessRuleException para evitar a dependência do Spring Security por enquanto.
                .orElseThrow(() -> new BusinessRuleException("CPF ou senha inválidos."));

        // 2. Validação de Status (Regra de Negócio).
        // Antes de verificar a senha, o sistema garante que a conta do usuário está ativa.
        // Lançar uma exceção específica aqui fornece uma mensagem de erro clara.
        if (!usuario.isAtivo()) {
            throw new BusinessRuleException("Este usuário está inativo e não pode acessar o sistema.");
        }

        // 3. Validação da Senha.
        // ATENÇÃO: Em um sistema de produção, a senha NUNCA deve ser comparada em texto plano.
        // O ideal é usar um PasswordEncoder do Spring Security. Ex:
        // if (!passwordEncoder.matches(request.senha(), usuario.getSenha())) { ... }
        // Para este projeto, a comparação direta é mantida para refletir a implementação atual.
        if (!usuario.getSenha().equals(request.senha())) {
            // Lança a mesma exceção do passo 1 para não informar a um potencial atacante
            // se o erro foi no usuário ou na senha (uma prática de segurança).
            throw new BusinessRuleException("CPF ou senha inválidos.");
        }

        // 4. Sucesso na Autenticação.
        // Se todas as validações passaram, cria um objeto de resposta (DTO) com os dados
        // necessários para o frontend iniciar a sessão do usuário.
        return new AuthResponse(
                usuario.getIdUsuario(),
                usuario.getNome(),
                usuario.getClass().getSimpleName().toUpperCase() // Ex: "FISIOTERAPEUTA", "ADMIN"
        );
    }
}
