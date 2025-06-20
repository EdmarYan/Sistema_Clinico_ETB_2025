package com.revitafisio.agendamento.repository;

import com.revitafisio.entities.agendamentos.Agendamento;
import com.revitafisio.entities.agendamentos.Agendamento.StatusAgendamento;
import com.revitafisio.funcionario.dto.RelatorioAtendimentoResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface de Repositório para a entidade Agendamento.
 * Estende JpaRepository para herdar operações CRUD padrão (save, findById, etc.)
 * e permite a definição de métodos de consulta customizados.
 *
 * Anotação:
 * @Repository: Marca esta interface como um bean do Spring, tornando-a elegível
 * para injeção de dependência e habilitando a tradução de exceções do banco de dados.
 */
@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    /**
     * Busca agendamentos de um fisioterapeuta específico dentro de um intervalo de data e hora.
     * Este é um metodo de consulta derivado (derived query method), onde o Spring Data JPA
     * automaticamente cria a query SQL baseada no nome do metodo.
     *
     * @param idFisioterapeuta ID do fisioterapeuta.
     * @param inicio Data e hora de início do período.
     * @param fim Data e hora de fim do período.
     * @return Uma lista de agendamentos que correspondem aos critérios.
     */
    List<Agendamento> findByFisioterapeuta_IdUsuarioAndDataHoraInicioGreaterThanEqualAndDataHoraFimLessThanEqual(
            Integer idFisioterapeuta, LocalDateTime inicio, LocalDateTime fim
    );

    /**
     * Gera um relatório de atendimentos realizados por período, agrupados por fisioterapeuta.
     *
     * Anotação:
     * @Query: Utilizada para definir uma consulta customizada usando a linguagem JPQL (Java Persistence Query Language).
     *
     * Detalhes da Query:
     * - `SELECT new com.revitafisio.funcionario.dto.RelatorioAtendimentoResponse(...)`: Esta é uma "expressão de construtor".
     * Em vez de retornar a entidade Agendamento inteira, a consulta cria diretamente um DTO (RelatorioAtendimentoResponse)
     * com os dados agregados (nome do fisioterapeuta e a contagem de agendamentos). Isso é muito mais performático.
     * - `JOIN a.fisioterapeuta u`: Faz a junção entre Agendamento (alias 'a') e Usuario (alias 'u').
     * - `WHERE a.status = 'REALIZADO' AND ...`: Filtra apenas os agendamentos realizados no período.
     * - `GROUP BY u.nome`: Agrupa os resultados pelo nome do fisioterapeuta para que o COUNT() funcione corretamente.
     *
     * @param inicio Data de início para o relatório.
     * @param fim Data de fim para o relatório.
     * @return Uma lista de DTOs com o nome de cada fisioterapeuta e seu total de atendimentos.
     */
    @Query("SELECT new com.revitafisio.funcionario.dto.RelatorioAtendimentoResponse(u.nome, COUNT(a)) " +
            "FROM Agendamento a JOIN a.fisioterapeuta u " +
            "WHERE a.status = 'REALIZADO' AND a.dataHoraInicio BETWEEN :inicio AND :fim " +
            "GROUP BY u.nome")
    List<RelatorioAtendimentoResponse> getRelatorioAtendimentosPorPeriodo(LocalDateTime inicio, LocalDateTime fim);

    /**
     * Busca todos os agendamentos com um status específico que ocorreram antes de uma data/hora.
     * Usado para encontrar agendamentos "pendentes" que já deveriam ter sido finalizados.
     *
     * @param status O status do agendamento a ser buscado (ex: CONFIRMADO).
     * @param data A data e hora de referência.
     * @return Uma lista de agendamentos.
     */
    List<Agendamento> findAllByStatusAndDataHoraInicioBefore(StatusAgendamento status, LocalDateTime data);
}
