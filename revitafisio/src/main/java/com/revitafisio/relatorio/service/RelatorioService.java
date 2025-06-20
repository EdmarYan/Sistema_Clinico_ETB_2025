package com.revitafisio.relatorio.service;

import com.revitafisio.funcionario.dto.RelatorioAtendimentoResponse;
import com.revitafisio.agendamento.repository.AgendamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

/**
 * Serviço que encapsula a lógica de negócio para a geração de relatórios.
 *
 * Anotação:
 * @Service: Marca esta classe como um componente de serviço do Spring,
 * contendo a lógica de negócio e sendo elegível para injeção de dependência.
 */
@Service
public class RelatorioService {

    private final AgendamentoRepository agendamentoRepository;

    public RelatorioService(AgendamentoRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
    }

    /**
     * Gera um relatório de atendimentos realizados, agrupados por fisioterapeuta,
     * para um determinado mês e ano.
     *
     * @param ano O ano do relatório.
     * @param mes O mês do relatório (1 a 12).
     * @return Uma lista de DTOs {@link RelatorioAtendimentoResponse}, onde cada DTO
     * representa um fisioterapeuta e seu total de atendimentos no período.
     */
    @Transactional(readOnly = true) // Otimização para consultas que não alteram o banco de dados.
    public List<RelatorioAtendimentoResponse> gerarRelatorioAtendimentos(int ano, int mes) {
        // 1. Usa a classe YearMonth do Java para obter o primeiro e o último dia do mês/ano informado.
        // Isso lida automaticamente com anos bissextos e meses de diferentes durações.
        YearMonth anoMes = YearMonth.of(ano, mes);

        // 2. Define o intervalo de busca preciso.
        // - atDay(1).atStartOfDay() pega o primeiro dia do mês, às 00:00:00.
        // - atEndOfMonth().atTime(23, 59, 59) pega o último dia do mês, às 23:59:59.
        LocalDateTime inicioDoMes = anoMes.atDay(1).atStartOfDay();
        LocalDateTime fimDoMes = anoMes.atEndOfMonth().atTime(23, 59, 59);

        // 3. Delega a busca para o repositório.
        // Vínculo: Chama o metodo de query JPQL customizada getRelatorioAtendimentosPorPeriodo
        // definido na interface AgendamentoRepository.
        return agendamentoRepository.getRelatorioAtendimentosPorPeriodo(inicioDoMes, fimDoMes);
    }
}
