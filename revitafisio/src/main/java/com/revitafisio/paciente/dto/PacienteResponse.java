package com.revitafisio.paciente.dto;

/**
 * DTO para transportar dados resumidos de um paciente.
 * Ideal para uso em listas, onde exibir todos os detalhes seria desnecessário.
 *
 * @param id O ID do paciente.
 * @param nome O nome do paciente.
 * @param cpf O CPF do paciente.
 */
public record PacienteResponse(
        Integer id,
        String nome,
        String cpf
) {
}
