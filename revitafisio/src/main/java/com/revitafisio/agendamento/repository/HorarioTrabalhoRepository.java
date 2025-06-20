package com.revitafisio.agendamento.repository;

import com.revitafisio.entities.agendamentos.HorarioTrabalho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

/**
 * Interface de Repositório para a entidade HorarioTrabalho.
 * Gerencia o acesso aos dados da grade de trabalho semanal dos fisioterapeutas.
 */
@Repository
public interface HorarioTrabalhoRepository extends JpaRepository<HorarioTrabalho, Integer> {

    /**
     * Busca toda a grade de trabalho cadastrada para um fisioterapeuta específico.
     * Vínculo: Usado pelo HorarioTrabalhoService para listar e gerar a agenda mensal.
     */
    List<HorarioTrabalho> findByFisioterapeutaIdUsuario(Integer idFisioterapeuta);

    /**
     * Busca a grade de trabalho de um fisioterapeuta para um dia específico da semana.
     * Vínculo: Usado pelo HorarioTrabalhoService para verificar conflitos antes de
     * adicionar um novo bloco de horário na grade.
     */
    List<HorarioTrabalho> findByFisioterapeuta_IdUsuarioAndDiaDaSemana(Integer idFisioterapeuta, DayOfWeek diaDaSemana);
}
