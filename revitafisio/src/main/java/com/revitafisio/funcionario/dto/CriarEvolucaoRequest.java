package com.revitafisio.funcionario.dto;

// DTO com os dados necessários para criar uma nova evolução.
public record CriarEvolucaoRequest(
        Integer idPaciente,
        Integer idFisioterapeuta,
        String descricao
) {
}