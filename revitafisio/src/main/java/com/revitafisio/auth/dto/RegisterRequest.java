package com.revitafisio.auth.dto;

import java.util.Set;

/**
 * DTO (Data Transfer Object) que representa os dados para registrar um novo usuário
 * no contexto de segurança (Spring Security).
 *
 * Embora não esteja sendo usado diretamente no AuthController atual, esta classe seria
 * fundamental em um sistema que utiliza o Spring Security para gerenciar papéis e permissões
 * de forma granular.
 *
 * @param cpf O CPF para o novo registro.
 * @param senha A senha para o novo usuário.
 * @param tipo O tipo de usuário a ser registrado (ex: "PACIENTE", "FISIOTERAPEUTA").
 * @param authorities Um conjunto de 'authorities' ou permissões (ex: "ROLE_ADMIN", "CAN_EDIT_PATIENT")
 * que seriam concedidas ao novo usuário.
 */
public record RegisterRequest(
        String cpf,
        String senha,
        String tipo,
        Set<String> authorities
) {}
