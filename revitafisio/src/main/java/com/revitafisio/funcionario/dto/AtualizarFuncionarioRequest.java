package com.revitafisio.funcionario.dto;

import java.time.LocalDate;

/**
 * DTO com os dados que podem ser atualizados de um funcionário.
 */
public record AtualizarFuncionarioRequest(
        String nome,
        LocalDate dataNascimento
) {
}