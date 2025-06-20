package com.revitafisio.funcionario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO (Data Transfer Object) com os dados necessários para criar uma nova evolução no prontuário.
 *
 * @param idPaciente ID do paciente ao qual a evolução pertence.
 * @param idFisioterapeuta ID do fisioterapeuta que está registrando a evolução.
 * @param descricao O texto descritivo da evolução da sessão.
 */
public record CriarEvolucaoRequest(
        @NotNull(message = "O ID do paciente é obrigatório.")
        Integer idPaciente,

        @NotNull(message = "O ID do fisioterapeuta é obrigatório.")
        Integer idFisioterapeuta,

        @NotBlank(message = "A descrição não pode estar em branco.")
        String descricao
) {}
