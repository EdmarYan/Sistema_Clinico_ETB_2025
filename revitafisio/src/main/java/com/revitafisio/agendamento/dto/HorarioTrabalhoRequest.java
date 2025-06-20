package com.revitafisio.agendamento.dto;

import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * DTO (Data Transfer Object) para receber os dados da requisição de criação
 * ou atualização de um item da grade de horários de um fisioterapeuta.
 *
 * @param idFisioterapeuta O ID do fisioterapeuta ao qual este horário pertence. Não pode ser nulo.
 * @param diaDaSemana O dia da semana para este bloco de horário. Não pode ser nulo.
 * @param horaInicio A hora de início do expediente. Não pode ser nula.
 * @param horaFim A hora de fim do expediente. Não pode ser nula.
 */
public record HorarioTrabalhoRequest(
        @NotNull(message = "O ID do fisioterapeuta é obrigatório.")
        Integer idFisioterapeuta,

        @NotNull(message = "O dia da semana é obrigatório.")
        DayOfWeek diaDaSemana,

        @NotNull(message = "A hora de início é obrigatória.")
        LocalTime horaInicio,

        @NotNull(message = "A hora de fim é obrigatória.")
        LocalTime horaFim
) {}
