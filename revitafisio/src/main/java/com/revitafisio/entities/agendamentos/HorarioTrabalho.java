package com.revitafisio.entities.agendamentos;

import com.revitafisio.entities.usuarios.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Representa a grade de trabalho semanal recorrente de um fisioterapeuta.
 * Cada registro define um bloco de tempo em um dia da semana em que o profissional está disponível
 * para atendimentos. (Ex: "Segunda-feira, das 08:00 às 12:00").
 *
 * Anotações:
 * @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor: Anotações do Lombok para código boilerplate.
 * @Entity e @Table: Mapeiam esta classe para a tabela "horarios_trabalho".
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "horarios_trabalho")
public class HorarioTrabalho {

    /**
     * Identificador único do registro de horário de trabalho.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_horario_trabalho")
    private Integer id;

    /**
     * O fisioterapeuta ao qual esta grade de horário pertence.
     */
    @NotNull(message = "O fisioterapeuta é obrigatório.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_fisioterapeuta", nullable = false)
    private Usuario fisioterapeuta;

    /**
     * O dia da semana para este bloco de horário (ex: MONDAY, TUESDAY).
     *
     * Persistência:
     * Este campo utiliza um `AttributeConverter` customizado (`DayOfWeekConverter`), que é aplicado
     * automaticamente em t0do o projeto. Esse conversor garante que o enum `DayOfWeek`
     * seja salvo no banco de dados como um número inteiro (1 para MONDAY, 7 para SUNDAY),
     * que é um formato mais padronizado e independente de linguagem do que salvar o nome do enum como string.
     */
    @NotNull(message = "O dia da semana é obrigatório.")
    @Column(name = "dia_semana", nullable = false)
    private DayOfWeek diaDaSemana;

    /**
     * A hora de início do bloco de trabalho.
     */
    @NotNull(message = "A hora de início é obrigatória.")
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    /**
     * A hora de término do bloco de trabalho.
     */
    @NotNull(message = "A hora de término é obrigatória.")
    @Column(name = "hora_fim", nullable = false)
    private LocalTime horaFim;

    /**
     * Flag para ativar ou desativar este bloco de horário sem precisar excluí-lo.
     */
    private boolean ativo = true;

    /**
     * Metodo de validação de regra de negócio.
     * Garante que a hora de término do expediente seja sempre posterior à hora de início.
     *
     * Tratamento de Erro:
     * A anotação @AssertTrue faz com que o Bean Validation execute este metodo
     * antes de salvar a entidade. Se o metodo retornar 'false', uma exceção
     * (ConstraintViolationException) será lançada com a mensagem especificada,
     * impedindo que dados inconsistentes sejam salvos no banco.
     *
     * @return true se o horário for válido, false caso contrário.
     */
    @AssertTrue(message = "A hora de término deve ser posterior à hora de início.")
    private boolean isHorarioValido() {
        // A validação só é necessária se ambos os horários não forem nulos.
        if (horaInicio != null && horaFim != null) {
            return horaFim.isAfter(horaInicio);
        }
        // Se um dos campos for nulo, a validação @NotNull cuidará disso.
        return true;
    }
}
