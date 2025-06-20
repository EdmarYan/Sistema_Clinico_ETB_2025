package com.revitafisio.auth.service;

import com.revitafisio.auth.dto.AuthRequest;
import com.revitafisio.auth.dto.AuthResponse;
import com.revitafisio.entities.usuarios.Usuario;
import com.revitafisio.entities.usuarios.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UsuarioRepository usuarioRepository;

    public AuthService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public AuthResponse autenticar(AuthRequest request) {
        // 1. Busca o usuário pelo CPF (ou email, dependendo da sua lógica)
        var usuario = usuarioRepository.findByCpf(request.cpf())
                .orElseThrow(() -> new RuntimeException("Credenciais inválidas"));

        // 2. ## VALIDAÇÃO DE STATUS ADICIONADA ##
        // Verifica se o usuário está ativo. Se não estiver, lança uma exceção
        // e impede o login, retornando uma mensagem clara.
        if (!usuario.isAtivo()) {
            throw new RuntimeException("Este usuário está inativo e não pode acessar o sistema.");
        }

        // 3. Se o usuário estiver ativo, prossegue para a verificação da senha
        if (!usuario.getSenha().equals(request.senha())) {
            throw new RuntimeException("Credenciais inválidas");
        }

        // 4. Se tudo estiver correto, retorna os dados para criar a sessão no frontend
        return new AuthResponse(
                usuario.getIdUsuario(),
                usuario.getNome(),
                usuario.getClass().getSimpleName().toUpperCase()
        );
    }
}
