package com.revitafisio.agendamento.dto;

import com.revitafisio.entities.agendamentos.HorarioTrabalho;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * DTO (Data Transfer Object) para exibir os dados de um item da grade de horários.
 *
 * @param id O ID único do registro de horário de trabalho.
 * @param diaDaSemana O enum DayOfWeek (ex: MONDAY).
 * @param nomeDiaSemana O nome do dia da semana por extenso em português (ex: "segunda-feira"),
 * para facilitar a exibição no frontend.
 * @param horaInicio A hora de início do expediente.
 * @param horaFim A hora de fim do expediente.
 */
public record HorarioTrabalhoResponse(
        Integer id,
        DayOfWeek diaDaSemana,
        String nomeDiaSemana,
        LocalTime horaInicio,
        LocalTime horaFim
) {
    /**
     * Construtor auxiliar que converte a entidade {@link HorarioTrabalho} para este DTO de resposta.
     * Além da simples cópia de dados, ele enriquece o DTO com o nome do dia da semana
     * formatado para o português do Brasil.
     *
     * @param horario A entidade HorarioTrabalho vinda do banco de dados.
     */
    public HorarioTrabalhoResponse(HorarioTrabalho horario) {
        this(
                horario.getId(),
                horario.getDiaDaSemana(),
                // Lógica para obter o nome do dia da semana em português
                horario.getDiaDaSemana().getDisplayName(TextStyle.FULL, new Locale("pt", "BR")),
                horario.getHoraInicio(),
                horario.getHoraFim()
        );
    }
}
