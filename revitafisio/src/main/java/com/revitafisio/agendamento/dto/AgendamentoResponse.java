package com.revitafisio.agendamento.dto;

import com.revitafisio.entities.agendamentos.Agendamento;
import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) para representar os dados de um agendamento
 * que serão enviados como resposta pela API.
 *
 * O uso de um DTO aqui é uma boa prática de segurança e design, pois garante que
 * apenas os dados necessários e seguros sejam expostos ao frontend, "achatando"
 * a estrutura complexa da entidade Agendamento em um formato simples.
 *
 * @param id O ID do agendamento.
 * @param nomePaciente O nome do paciente associado.
 * @param nomeFisioterapeuta O nome do fisioterapeuta responsável.
 * @param nomeEspecialidade O nome da especialidade do agendamento.
 * @param inicio A data e hora de início.
 * @param fim A data e hora de término.
 * @param status O status atual do agendamento (ex: "CONFIRMADO").
 */
public record AgendamentoResponse(
        Integer id,
        String nomePaciente,
        String nomeFisioterapeuta,
        String nomeEspecialidade,
        LocalDateTime inicio,
        LocalDateTime fim,
        String status
) {
    /**
     * Construtor auxiliar para converter facilmente uma entidade {@link Agendamento}
     * (vinda do banco de dados) para este DTO de resposta.
     *
     * Este construtor extrai apenas as informações necessárias das entidades relacionadas
     * (Paciente, Fisioterapeuta, Especialidade), evitando expor a estrutura interna completa.
     *
     * @param agendamento A entidade JPA Agendamento a ser convertida.
     */
    public AgendamentoResponse(Agendamento agendamento) {
        this(
                agendamento.getIdAgendamento(),
                agendamento.getPaciente().getNome(),
                agendamento.getFisioterapeuta().getNome(),
                agendamento.getEspecialidade().getNome(),
                agendamento.getDataHoraInicio(),
                agendamento.getDataHoraFim(),
                // Tratamento de Erro: Garante que, se o status for nulo por algum motivo,
                // a aplicação não quebre com um NullPointerException ao tentar chamar .name().
                (agendamento.getStatus() != null) ? agendamento.getStatus().name() : "INDEFINIDO"
        );
    }
}
