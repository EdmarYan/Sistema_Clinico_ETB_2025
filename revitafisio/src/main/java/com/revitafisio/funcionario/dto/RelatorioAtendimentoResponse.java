package com.revitafisio.funcionario.dto;

// DTO para a resposta do relatório de atendimentos.
public record RelatorioAtendimentoResponse(
        String nomeFisioterapeuta,
        Long totalAtendimentos
) {
}