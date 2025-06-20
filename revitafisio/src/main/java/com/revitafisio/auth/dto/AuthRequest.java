package com.revitafisio.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO (Data Transfer Object) que representa os dados de uma requisição de login.
 *
 * Utiliza o recurso 'record' do Java, que é ideal para criar classes imutáveis
 * destinadas a carregar dados. O compilador gera automaticamente o construtor,
 * getters, equals(), hashCode() e toString().
 *
 * @param cpf O CPF fornecido pelo usuário para autenticação.
 * @param senha A senha fornecida pelo usuário.
 */
public record AuthRequest(
        /**
         * O CPF do usuário.
         * Anotação de Validação:
         * @NotBlank: Garante que o CPF não pode ser nulo nem conter apenas espaços em branco.
         * Se a requisição chegar com este campo vazio, a API retornará um erro 400 (Bad Request)
         * antes mesmo de a lógica de negócio ser executada.
         */
        @NotBlank(message = "O CPF é obrigatório.")
        String cpf,

        /**
         * A senha do usuário.
         *
         * Anotação de Validação:
         * @NotBlank: Garante que a senha não pode ser nula ou vazia.
         */
        @NotBlank(message = "A senha é obrigatória.")
        String senha
) {}
