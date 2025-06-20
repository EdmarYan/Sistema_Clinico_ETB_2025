package com.revitafisio.funcionario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * DTO que define os dados necessários para criar qualquer tipo de funcionário.
 *
 * @param nome Nome completo do funcionário.
 * @param cpf CPF do funcionário.
 * @param dataNascimento Data de nascimento.
 * @param senha Senha inicial de acesso.
 * @param tipo O tipo de funcionário a ser criado (FISIOTERAPEUTA, RECEPCIONISTA, ADMIN).
 */
public record CriarFuncionarioRequest(
        @NotBlank(message = "O nome é obrigatório.")
        String nome,

        @NotBlank(message = "O CPF é obrigatório.")
        @Size(min = 11, max = 14, message = "CPF deve ter entre 11 e 14 caracteres.")
        String cpf,

        LocalDate dataNascimento,

        @NotBlank(message = "A senha é obrigatória.")
        String senha,

        @NotNull(message = "O tipo de funcionário é obrigatório.")
        TipoFuncionario tipo
) {
    /**
     * Enum para definir os tipos de funcionários possíveis que podem ser criados.
     * Garante que apenas valores válidos sejam enviados na requisição.
     */
    public enum TipoFuncionario {
        FISIOTERAPEUTA,
        RECEPCIONISTA,
        ADMIN
    }
    @Override
    public String toString() {
        return "CriarFuncionarioRequest{" +
                "nome='" + nome + '\'' +
                ", cpf='" + cpf + '\'' +
                ", dataNascimento=" + dataNascimento +
                ", senha='[PROTEGIDO]'" +
                ", tipo=" + tipo +
                '}';
    }
}
