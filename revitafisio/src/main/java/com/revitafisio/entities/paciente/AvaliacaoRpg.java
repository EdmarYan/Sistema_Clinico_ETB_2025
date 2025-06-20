package com.revitafisio.entities.paciente;

import com.revitafisio.entities.usuarios.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * Entidade que mapeia a ficha de avaliação específica para a especialidade de RPG.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "avaliacao_rpg")
public class AvaliacaoRpg {

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
    @Column(length = 255) private String diagnostico_clinico;
    @Lob @Column(columnDefinition = "TEXT") private String hma;
    @Column(length = 255) private String posicao_dor;
    @Lob @Column(columnDefinition = "TEXT") private String outras_patologias;
    @Column(length = 255) private String outros_exames;
    @Column(length = 255) private String medicamentos_descricao;
    @Lob @Column(columnDefinition = "TEXT") private String outros_desequilibrios;
    @Lob @Column(columnDefinition = "TEXT") private String tratamento_proposto;
    @Lob @Column(columnDefinition = "TEXT") private String observacoes;

    // --- MELHORIA: de 'boolean' primitivo para 'Boolean' wrapper ---
    // Mudar para o tipo wrapper (Boolean) é uma prática recomendada para campos de entidade
    // que representam checkboxes ou opções que podem não ser preenchidas.
    // Isso permite que o valor no banco de dados seja NULL, representando "não informado",
    // o que é diferente de 'false' (explicitamente marcado como não).
    private Boolean ressonancia_magnetica;
    private Boolean raio_x;
    private Boolean tomografia;
    private Boolean uso_medicamentos;

    // --- Campos com Enums (garantem a consistência dos dados) ---
    @Enumerated(EnumType.STRING) private GrauDor grau_dor;
    @Enumerated(EnumType.STRING) private PosicaoCabeca cabeca;
    @Enumerated(EnumType.STRING) private NivelamentoOmbros ombros;
    @Enumerated(EnumType.STRING) private SimetriaMaos maos;
    @Enumerated(EnumType.STRING) private SimetriaEias eias;
    @Enumerated(EnumType.STRING) private PosicaoJoelhos joelhos;
    @Enumerated(EnumType.STRING) private CurvaturaLombar lombar;
    @Enumerated(EnumType.STRING) private PosicaoPelve pelve;
    @Enumerated(EnumType.STRING) private PosicaoEscapulas escapulas;

    // Definição dos Enums para esta avaliação
    public enum GrauDor { LEVE, MODERADA, INTENSA }
    public enum PosicaoCabeca { ALINHADA, RODADA_DIREITA, RODADA_ESQUERDA, INCLINADA_ESQUERDA, INCLINADA_DIREITA }
    public enum NivelamentoOmbros { NIVELADOS, ESQUERDO_ELEVADO, DIREITO_ELEVADO }
    public enum SimetriaMaos { SIMETRICOS, DIREITA_ALTA, ESQUERDA_ALTA }
    public enum SimetriaEias { SIMETRICAS, DIREITA_ALTA, ESQUERDA_ALTA }
    public enum PosicaoJoelhos { VALGO, VARO, NORMAL }
    public enum CurvaturaLombar { HIPERLORDOSE, RETIFICADA, NORMAL }
    public enum PosicaoPelve { ANTEVERSÃO, RETROVERSÃO, NORMAL }
    public enum PosicaoEscapulas { DIREITA_ALTA, ESQUERDA_ALTA }
}
