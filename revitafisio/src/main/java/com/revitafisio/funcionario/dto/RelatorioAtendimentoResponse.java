package com.revitafisio.funcionario.dto;

/**
 * DTO de Projeção para representar os dados do relatório de atendimentos.
 *
 * Uma projeção é um objeto que carrega um subconjunto de dados retornados
 * por uma consulta customizada, em vez da entidade completa.
 *
 * Vínculo: Este DTO é construído diretamente pela query JPQL definida em
 * {@link com.revitafisio.agendamento.repository.AgendamentoRepository#getRelatorioAtendimentosPorPeriodo(java.time.LocalDateTime, java.time.LocalDateTime)}.
 * O uso de `SELECT new ...` na query instancia este record para cada resultado,
 * o que é muito mais performático do que buscar entidades inteiras para depois agregar os dados.
 *
 * @param nomeFisioterapeuta O nome do fisioterapeuta.
 * @param totalAtendimentos A contagem total de atendimentos realizados por ele no período.
 */
public record RelatorioAtendimentoResponse(
        String nomeFisioterapeuta,
        Long totalAtendimentos
) {
}
