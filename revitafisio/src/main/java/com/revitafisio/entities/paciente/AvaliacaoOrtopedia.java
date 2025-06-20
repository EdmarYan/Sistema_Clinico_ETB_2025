package com.revitafisio.entities.paciente;

import com.revitafisio.entities.usuarios.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidade que mapeia a ficha de avaliação específica para a especialidade de Ortopedia.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "avaliacao_ortopedia")
public class AvaliacaoOrtopedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_avaliacao")
    private Integer idAvaliacao;

    @NotNull(message = "O paciente da avaliação não pode ser nulo.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paciente", nullable = false)
    private Usuario paciente;

    @NotNull(message = "O fisioterapeuta da avaliação não pode ser nulo.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_fisioterapeuta", nullable = false)
    private Usuario fisioterapeuta;

    @NotNull(message = "A data da avaliação é obrigatória.")
    @PastOrPresent(message = "A data da avaliação não pode ser no futuro.")
    @Column(name = "data_avaliacao", nullable = false)
    private LocalDate dataAvaliacao;

    // --- Campos de Texto ---
    @Column(length = 255) private String profissao;
    @Column(length = 255) private String pressao_arterial;
    @Column(length = 255) private String avaliacao_postural;

    // Anotação @Lob é usada para campos que podem conter textos muito longos,
    // garantindo que o JPA os mapeie para um tipo de coluna apropriado (TEXT, LONGTEXT, etc.).
    @Lob @Column(columnDefinition = "LONGTEXT") private String alergias;
    @Lob @Column(columnDefinition = "LONGTEXT") private String indicacao_medica;
    @Lob @Column(columnDefinition = "LONGTEXT") private String queixa_principal;
    @Lob @Column(columnDefinition = "LONGTEXT") private String hda_hdp;
    @Lob @Column(columnDefinition = "LONGTEXT") private String doencas_cardiacas;
    @Lob @Column(columnDefinition = "LONGTEXT") private String comorbidades;
    @Lob @Column(columnDefinition = "LONGTEXT") private String medicacoes;
    @Lob @Column(columnDefinition = "LONGTEXT") private String diagnostico_fisioterapeutico;
    @Lob @Column(columnDefinition = "LONGTEXT") private String objetivos;
    @Lob @Column(columnDefinition = "LONGTEXT") private String conduta;
    @Lob @Column(columnDefinition = "LONGTEXT") private String observacoes;

    // --- Campos Numéricos ---
    private Integer frequencia_cardiaca;
    private Integer frequencia_respiratoria;

    /**
     * Temperatura do paciente. O uso de BigDecimal é a melhor prática para
     * valores monetários ou que exigem alta precisão decimal, evitando
     * problemas de arredondamento inerentes aos tipos float e double.
     */
    @Column(precision = 5, scale = 2) // Ex: 999.99
    private BigDecimal temperatura;
}
