package com.revitafisio.paciente.dto;

import com.revitafisio.entities.paciente.AvaliacaoRpg;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO com todos os campos do formulário para criar ou atualizar
 * uma avaliação de RPG.
 *
 * @param idPaciente ID do paciente avaliado.
 * @param idFisioterapeuta ID do fisioterapeuta que realiza a avaliação.
 * @param diagnostico_clinico Diagnóstico clínico, campo obrigatório.
 * @param tratamento_proposto Tratamento proposto, campo obrigatório.
 * @param //... outros campos da ficha de avaliação de RPG.
 */
public record AvaliacaoRpgRequest(
        @NotNull(message = "O ID do paciente é obrigatório.")
        Integer idPaciente,

        @NotNull(message = "O ID do fisioterapeuta é obrigatório.")
        Integer idFisioterapeuta,

        @NotBlank(message = "O diagnóstico clínico é obrigatório.")
        String diagnostico_clinico,

        String hma,
        String posicao_dor,
        String outras_patologias,
        String outros_exames,
        String medicamentos_descricao,
        String outros_desequilibrios,

        @NotBlank(message = "O tratamento proposto é obrigatório.")
        String tratamento_proposto,

        String observacoes,
        Boolean ressonancia_magnetica,
        Boolean raio_x,
        Boolean tomografia,
        Boolean uso_medicamentos,
        AvaliacaoRpg.GrauDor grau_dor,
        AvaliacaoRpg.PosicaoCabeca cabeca,
        AvaliacaoRpg.NivelamentoOmbros ombros,
        AvaliacaoRpg.SimetriaMaos maos,
        AvaliacaoRpg.SimetriaEias eias,
        AvaliacaoRpg.PosicaoJoelhos joelhos,
        AvaliacaoRpg.CurvaturaLombar lombar,
        AvaliacaoRpg.PosicaoPelve pelve,
        AvaliacaoRpg.PosicaoEscapulas escapulas
) {
}
