package com.revitafisio.entities.agendamentos;

import com.revitafisio.entities.usuarios.Especialidade;
import com.revitafisio.entities.usuarios.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Representa um agendamento de consulta ou sessão de fisioterapia.
 * Esta é uma das entidades mais centrais do sistema, pois conecta um Paciente,
 * um Fisioterapeuta e uma Especialidade em um evento com data e hora específicas.
 *
 * Anotações:
 * @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor: Anotações do Lombok para código boilerplate.
 * @Entity e @Table: Mapeiam esta classe para a tabela "agendamentos" no banco de dados.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "agendamentos")
public class Agendamento {

    /**
     * Identificador único do agendamento.
     * O tipo foi mudado para Long no DDL, mas a classe usa Integer. O ideal é manter consistência.
     * Manterei Integer para seguir a classe original, mas o ideal seria Long.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_agendamento")
    private Integer idAgendamento;

    /**
     * O paciente para quem a consulta foi agendada.
     * O tipo é Usuario para manter a flexibilidade do relacionamento, mas na prática,
     * a lógica de negócio deve garantir que este usuário seja do tipo Paciente.
     */
    @NotNull(message = "O paciente do agendamento é obrigatório.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paciente", nullable = false)
    private Usuario paciente;

    /**
     * O fisioterapeuta que realizará o atendimento.
     */
    @NotNull(message = "O fisioterapeuta do agendamento é obrigatório.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_fisioterapeuta", nullable = false)
    private Usuario fisioterapeuta;

    /**
     * A especialidade relacionada ao agendamento (ex: Ortopedia, Pilates).
     */
    @NotNull(message = "A especialidade do agendamento é obrigatória.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_especialidade", nullable = false)
    private Especialidade especialidade;

    /**
     * Data e hora de início da consulta.
     *
     * Anotação de Validação:
     * @Future: Garante que um agendamento só possa ser criado para uma data e hora no futuro.
     */
    @NotNull(message = "A data e hora de início são obrigatórias")
    @Future(message = "O horário de início deve ser futuro")
    @Column(name = "data_hora_inicio", nullable = false)
    private LocalDateTime dataHoraInicio;

    /**
     * Data e hora de término da consulta.
     */
    @NotNull(message = "A data e hora de término são obrigatórias")
    @Future(message = "O horário de término deve ser futuro")
    @Column(name = "data_hora_fim", nullable = false)
    private LocalDateTime dataHoraFim;

    /**
     * O status atual do agendamento, controlado pelo enum StatusAgendamento.
     *
     * Anotação:
     * @Enumerated(EnumType.STRING): Persiste o nome do enum ("CONFIRMADO", "CANCELADO", etc.)
     * como uma String no banco, tornando os dados mais legíveis.
     */
    @NotNull(message = "O status do agendamento é obrigatório.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAgendamento status;

    /**
     * Enum que define os possíveis estados de um agendamento no sistema.
     * O uso de um Enum garante a consistência e previne a inserção de status inválidos.
     */
    public enum StatusAgendamento {
        CONFIRMADO,
        CANCELADO,
        PENDENTE,
        REALIZADO,
        NAO_COMPARECEU
    }

    /**
     * Metodo de validação de regra de negócio para consistência do horário do agendamento.
     *
     * Tratamento de Erro:
     * A anotação @AssertTrue garante que, antes de salvar, a data/hora de término
     * seja sempre posterior à data/hora de início.
     *
     * @return true se o intervalo de tempo for válido, false caso contrário.
     */
    @AssertTrue(message = "A data e hora de término devem ser posteriores à data e hora de início.")
    private boolean isHorarioValido() {
        if (dataHoraInicio != null && dataHoraFim != null) {
            return dataHoraFim.isAfter(dataHoraInicio);
        }
        return true;
    }
}
