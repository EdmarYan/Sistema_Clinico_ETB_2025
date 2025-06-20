package com.revitafisio.agendamento.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) que representa os dados necessários para criar um novo agendamento.
 *
 * @param idPaciente ID do paciente. Não pode ser nulo.
 * @param idFisioterapeuta ID do fisioterapeuta. Não pode ser nulo.
 * @param idEspecialidade ID da especialidade. Não pode ser nulo.
 * @param dataHoraInicio Data e hora de início do agendamento. Deve ser no futuro.
 * @param dataHoraFim Data e hora de fim do agendamento. Deve ser no futuro.
 */
public record CriarAgendamentoRequest(
        @NotNull(message = "O ID do paciente é obrigatório.")
        Integer idPaciente,

        @NotNull(message = "O ID do fisioterapeuta é obrigatório.")
        Integer idFisioterapeuta,

        @NotNull(message = "O ID da especialidade é obrigatório.")
        Integer idEspecialidade,

        @NotNull(message = "A data e hora de início são obrigatórias.")
        @Future(message = "A data de início do agendamento deve ser no futuro.")
        LocalDateTime dataHoraInicio,

        @NotNull(message = "A data e hora de término são obrigatórias.")
        @Future(message = "A data de término do agendamento deve ser no futuro.")
        LocalDateTime dataHoraFim
) {}
