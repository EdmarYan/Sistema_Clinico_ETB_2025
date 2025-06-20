package com.revitafisio.funcionario.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO com os dados necessários para criar um novo paciente.
 * Note que este DTO não inclui o campo "senha", pois pacientes não acessam o sistema interno.
 *
 * @param nome Nome completo do paciente.
 * @param cpf CPF do paciente, para identificação única.
 * @param dataNascimento Data de nascimento do paciente.
 * @param contatos Uma lista de contatos a serem criados junto com o paciente.
 * A anotação @Valid ativa a validação para cada item dentro desta lista.
 */
public record CriarPacienteRequest(
        @NotBlank(message = "O nome do paciente é obrigatório.")
        String nome,

        @NotBlank(message = "O CPF do paciente é obrigatório.")
        String cpf,

        @Past(message = "A data de nascimento deve ser no passado.")
        LocalDate dataNascimento,

        @Valid // Essencial para validar os objetos CriarContatoRequest dentro da lista
        List<CriarContatoRequest> contatos
) {}
