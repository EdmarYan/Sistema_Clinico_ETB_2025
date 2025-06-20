package com.revitafisio.agendamento.dto;

import com.revitafisio.entities.agendamentos.HorarioDisponivel;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO (Data Transfer Object) para transportar os dados de um horário disponível
 * que será enviado como resposta pela API.
 *
 * @param id O ID único do slot de horário.
 * @param data A data específica do horário.
 * @param horaInicio A hora de início do slot.
 * @param horaFim A hora de fim do slot.
 * @param disponivel Um booleano indicando se o horário ainda está disponível.
 */
public record HorarioDisponivelResponse(
        long id,
        LocalDate data,
        LocalTime horaInicio,
        LocalTime horaFim,
        boolean disponivel
) {
    /**
     * Construtor auxiliar que converte a entidade {@link HorarioDisponivel}
     * para este DTO de resposta.
     * @param horario A entidade HorarioDisponivel vinda do banco de dados.
     */
    public HorarioDisponivelResponse(HorarioDisponivel horario) {
        this(
                horario.getIdHorario(),
                horario.getData(),
                horario.getHoraInicio(),
                horario.getHoraFim(),
                horario.isDisponivel()
        );
    }
}
