package com.revitafisio.paciente.dto;

public record PacienteResponse(
            Integer id,
            String nome,
            String cpf
    ) {
}
