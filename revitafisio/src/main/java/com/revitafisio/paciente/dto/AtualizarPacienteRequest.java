package com.revitafisio.paciente.dto;

import com.revitafisio.funcionario.dto.CriarContatoRequest;

import java.time.LocalDate;
import java.util.List;

// Dados que a API recebe para ATUALIZAR um paciente. Note que não pedimos CPF ou senha.
public record AtualizarPacienteRequest(
        String nome,
        LocalDate dataNascimento,
        List<CriarContatoRequest> contatos
) {
}