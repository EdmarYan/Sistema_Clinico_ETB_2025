package com.revitafisio.agendamento.dto;

import com.revitafisio.entities.agendamentos.HorarioDisponivel;
import java.time.LocalDate; // Verifique se este import está presente
import java.time.LocalTime;

/**
 * DTO (Record) para transportar os dados de um horário disponível.
 */
public record HorarioDisponivelResponse(
        long id,
        LocalDate data, // <-- CAMPO ADICIONADO E CORRIGIDO
        LocalTime horaInicio,
        LocalTime horaFim,
        boolean disponivel
) {
    /**
     * Construtor que converte a entidade do banco de dados para este DTO de resposta.
     * @param horario A entidade HorarioDisponivel.
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