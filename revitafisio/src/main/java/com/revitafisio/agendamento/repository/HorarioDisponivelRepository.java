package com.revitafisio.agendamento.repository;

import com.revitafisio.entities.agendamentos.HorarioDisponivel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Interface de Repositório para a entidade HorarioDisponivel.
 * Gerencia o acesso aos dados dos slots de horários disponíveis.
 */
@Repository
public interface HorarioDisponivelRepository extends JpaRepository<HorarioDisponivel, Integer> {

    /**
     * Busca todos os horários disponíveis para um fisioterapeuta em um intervalo de datas.
     * Vínculo: Usado pelo HorarioDisponivelService para exibir a agenda.
     */
    List<HorarioDisponivel> findByFisioterapeutaIdUsuarioAndDataBetween(Integer idFisioterapeuta, LocalDate dataInicio, LocalDate dataFim);

    /**
     * Deleta todos os horários disponíveis de um fisioterapeuta em um intervalo de datas.
     * Este é um metodo crucial para a funcionalidade de "gerar agenda", pois limpa os horários
     * antigos antes de criar os novos.
     * Vínculo: Usado pelo HorarioTrabalhoService no metodo gerarDisponibilidadeParaMes.
     */
    void deleteByFisioterapeutaIdUsuarioAndDataBetween(Integer idFisioterapeuta, LocalDate dataInicio, LocalDate dataFim);

    /**
     * Busca um slot de horário específico por fisioterapeuta, data e hora de início.
     * Essencial para o processo de criação de um agendamento, para encontrar e "travar"
     * o slot de horário que está sendo agendado.
     * Vínculo: Usado pelo AgendamentoService no metodo criarAgendamento.
     */
    List<HorarioDisponivel> findByFisioterapeutaIdUsuarioAndDataAndHoraInicio(
            Integer idFisioterapeuta,
            LocalDate data,
            LocalTime horaInicio
    );
}
