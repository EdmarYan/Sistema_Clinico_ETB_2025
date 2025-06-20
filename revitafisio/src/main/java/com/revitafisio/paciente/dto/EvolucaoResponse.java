package com.revitafisio.paciente.dto;

import com.revitafisio.entities.paciente.Evolucao;

import java.time.LocalDate;

/**
 * DTO para enviar os dados de um registro de evolução para o frontend.
 *
 * @param idEvolucao O ID único do registro de evolução.
 * @param data A data em que a evolução foi registrada.
 * @param descricao O texto descritivo da evolução.
 * @param nomeFisioterapeuta O nome do fisioterapeuta que registrou, para fácil exibição.
 */
public record EvolucaoResponse(
        Integer idEvolucao,
        LocalDate data,
        String descricao,
        String nomeFisioterapeuta
) {
    /**
     * Construtor auxiliar para facilitar a conversão da entidade {@link Evolucao} para este DTO.
     * Simplifica a lógica na camada de serviço.
     * @param evolucao A entidade Evolucao a ser convertida.
     */
    public EvolucaoResponse(Evolucao evolucao) {
        this(
                evolucao.getIdEvolucao(),
                evolucao.getData(),
                evolucao.getDescricao(),
                evolucao.getFisioterapeuta().getNome() // Extrai o nome do fisioterapeuta associado
        );
    }
}
