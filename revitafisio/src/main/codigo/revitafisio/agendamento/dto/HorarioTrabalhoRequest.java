package com.revitafisio.agendamento.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * DTO para criar ou atualizar um item da grade de horários de um fisioterapeuta.
 */
public record HorarioTrabalhoRequest(
        Integer idFisioterapeuta,
        DayOfWeek diaDaSemana,
        LocalTime horaInicio,
        LocalTime horaFim
) {
}