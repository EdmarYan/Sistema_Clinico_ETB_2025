package com.revitafisio.auth.dto;

/**
 * DTO (Data Transfer Object) que representa a resposta de uma autenticação bem-sucedida.
 *
 * Esta classe 'record' carrega os dados essenciais sobre o usuário autenticado, que serão
 * enviados de volta para o frontend para que a sessão possa ser iniciada.
 * Note que informações sensíveis, como a senha, NUNCA são incluídas na resposta.
 *
 * @param usuarioId O ID único do usuário, útil para futuras requisições.
 * @param nome O nome do usuário, para ser exibido na interface.
 * @param tipoUsuario O tipo de usuário (ex: "ADMIN", "FISIOTERAPEUTA"), para que o frontend
 * possa renderizar o menu e as funcionalidades corretas para aquele perfil.
 */
public record AuthResponse(
        Integer usuarioId,
        String nome,
        String tipoUsuario
) {}
