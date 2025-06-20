package com.revitafisio.funcionario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

/**
 * DTO (Data Transfer Object) para receber os dados da requisição
 * de atualização dos dados cadastrais de um funcionário.
 *
 * @param nome O novo nome completo do funcionário. Não pode ser vazio.
 * @param dataNascimento A nova data de nascimento. Deve ser uma data no passado.
 */
public record AtualizarFuncionarioRequest(
        @NotBlank(message = "O nome não pode estar em branco.")
        String nome,

        @Past(message = "A data de nascimento deve ser no passado.")
        LocalDate dataNascimento
) {}
