package com.revitafisio.entities.agendamentos;

import com.revitafisio.entities.usuarios.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Representa um "slot" ou intervalo de tempo específico que está disponível para agendamento.
 *
 * Esta entidade é gerada a partir da grade de {@link HorarioTrabalho} de um fisioterapeuta.
 * Enquanto HorarioTrabalho define o padrão semanal ("toda segunda-feira de 08h às 12h"),
 * HorarioDisponivel representa a instância concreta disso ("segunda-feira, dia 23/06/2025, das 08:00 às 08:30").
 *
 * Anotações:
 * @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor: Anotações do Lombok para código boilerplate.
 * @Entity e @Table: Mapeiam esta classe para a tabela "horarios_disponiveis".
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "horarios_disponiveis")
public class HorarioDisponivel {

    /**
     * Identificador único do slot de horário.
     * Usar Long é uma boa prática para chaves primárias de tabelas que podem crescer muito.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_horario")
    private Long idHorario;

    /**
     * O fisioterapeuta ao qual este horário disponível pertence.
     */
    @NotNull(message = "O fisioterapeuta do horário é obrigatório.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_fisioterapeuta", nullable = false)
    private Usuario fisioterapeuta;

    /**
     * A data específica para este slot de horário.
     *
     * Anotação de Validação:
     * @FutureOrPresent: Garante que o sistema não possa gerar horários disponíveis para datas passadas.
     */
    @NotNull(message = "A data do horário é obrigatória.")
    @FutureOrPresent(message = "A data do horário disponível não pode ser no passado.")
    @Column(nullable = false)
    private LocalDate data;

    /**
     * A hora de início do slot de agendamento.
     */
    @NotNull(message = "A hora de início é obrigatória.")
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    /**
     * A hora de término do slot de agendamento.
     */
    @NotNull(message = "A hora de término é obrigatória.")
    @Column(name = "hora_fim", nullable = false)
    private LocalTime horaFim;

    /**
     * Flag que indica se este slot está atualmente disponível.
     * Este campo deve ser 'true' quando o slot é criado e se tornar 'false'
     * quando um agendamento é confirmado para este horário.
     */
    private boolean disponivel;

    /**
     * Metodo de validação de regra de negócio para consistência do horário.
     *
     * Tratamento de Erro:
     * A anotação @AssertTrue garante que, antes de salvar, a hora de término do slot
     * seja sempre posterior à hora de início, prevenindo a criação de dados inválidos.
     *
     * @return true se o horário for válido, false caso contrário.
     */
    @AssertTrue(message = "A hora de término do slot deve ser posterior à hora de início.")
    private boolean isHorarioValido() {
        if (horaInicio != null && horaFim != null) {
            return horaFim.isAfter(horaInicio);
        }
        return true;
    }
}
