package com.revitafisio.entities.paciente;

import com.revitafisio.entities.usuarios.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * Representa um registro de evolução do tratamento de um paciente.
 * Cada instância desta classe corresponde a uma entrada no histórico clínico,
 * descrevendo o que foi feito em uma sessão específica.
 *
 * Anotações:
 * @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor: Anotações do Lombok.
 * @Entity e @Table: Mapeiam esta classe para a tabela "evolucao" no banco de dados.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "evolucao")
@Table(name = "evolucao")
public class Evolucao {

    /**
     * Identificador único do registro de evolução.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evolucao")
    private Integer idEvolucao;

    /**
     * O paciente ao qual este registro de evolução pertence.
     *
     * Anotação de Validação:
     * @NotNull: Garante que uma evolução não pode ser criada sem um paciente associado.
     */
    @NotNull(message = "O paciente da evolução não pode ser nulo.")
    @ManyToOne(fetch = FetchType.LAZY) // LAZY é o padrão para ManyToOne
    @JoinColumn(name = "id_paciente", nullable = false)
    private Usuario paciente;

    /**
     * O fisioterapeuta que realizou o atendimento e registrou a evolução.
     *
     * Anotação de Validação:
     * @NotNull: Garante que uma evolução não pode ser criada sem um fisioterapeuta responsável.
     */
    @NotNull(message = "O fisioterapeuta da evolução não pode ser nulo.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_fisioterapeuta", nullable = false)
    private Usuario fisioterapeuta;

    /**
     * A data em que a sessão de tratamento ocorreu.
     *
     * Anotação de Validação:
     * @PastOrPresent: Garante que a data da evolução não pode ser uma data futura.
     */
    @NotNull(message = "A data da evolução é obrigatória.")
    @PastOrPresent(message = "A data da evolução não pode ser no futuro.")
    @Column(nullable = false)
    private LocalDate data;

    /**
     * A descrição detalhada do atendimento, incluindo técnicas aplicadas,
     * progresso do paciente e observações.
     *
     * Anotações:
     * @Lob: Indica ao JPA que este campo deve ser tratado como um "Large Object",
     * o que é apropriado para campos de texto longos.
     * @Column(columnDefinition = "TEXT"): Define explicitamente o tipo da coluna no banco de dados
     * como TEXT, garantindo compatibilidade e capacidade para armazenar textos extensos.
     * @NotBlank: Valida que a descrição não pode ser vazia.
     */
    @NotBlank(message = "A descrição da evolução não pode estar em branco.")
    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String descricao;

    /**
     * Um campo de controle que poderia ser usado para indicar se a evolução
     * foi formalmente revisada ou "fechada".
     * No DDL, o padrão é '0' (false).
     */
    private boolean preenchida;
}
