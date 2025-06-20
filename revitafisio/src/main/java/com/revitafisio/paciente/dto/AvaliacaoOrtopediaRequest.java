package com.revitafisio.paciente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * DTO com todos os campos do formulário para criar ou atualizar
 * uma avaliação de Ortopedia.
 *
 * @param idPaciente ID do paciente avaliado.
 * @param idFisioterapeuta ID do fisioterapeuta que realiza a avaliação.
 * @param queixa_principal A queixa principal do paciente, campo obrigatório.
 * @param diagnostico_fisioterapeutico O diagnóstico profissional, campo obrigatório.
 * @param //... outros campos da ficha de avaliação.
 */
public record AvaliacaoOrtopediaRequest(
        @NotNull(message = "O ID do paciente é obrigatório.")
        Integer idPaciente,

        @NotNull(message = "O ID do fisioterapeuta é obrigatório.")
        Integer idFisioterapeuta,

        String profissao,
        String pressao_arterial,
        String avaliacao_postural,
        String alergias,
        String indicacao_medica,

        @NotBlank(message = "A queixa principal é obrigatória.")
        String queixa_principal,

        String hda_hdp,
        String doencas_cardiacas,
        String comorbidades,
        String medicacoes,

        @NotBlank(message = "O diagnóstico fisioterapêutico é obrigatório.")
        String diagnostico_fisioterapeutico,

        String objetivos,
        String conduta,
        String observacoes,
        Integer frequencia_cardiaca,
        Integer frequencia_respiratoria,
        BigDecimal temperatura
) {
}
