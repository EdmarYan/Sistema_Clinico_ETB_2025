package com.revitafisio.agendamento.service;

import com.revitafisio.agendamento.dto.HorarioTrabalhoRequest;
import com.revitafisio.agendamento.dto.HorarioTrabalhoResponse;
import com.revitafisio.agendamento.repository.HorarioDisponivelRepository;
import com.revitafisio.agendamento.repository.HorarioTrabalhoRepository;
import com.revitafisio.entities.agendamentos.HorarioDisponivel;
import com.revitafisio.entities.agendamentos.HorarioTrabalho;
import com.revitafisio.exception.BusinessRuleException;
import com.revitafisio.exception.ResourceNotFoundException;
import com.revitafisio.funcionario.repository.FuncionarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço que encapsula a lógica de negócio para a gestão da grade de horários de trabalho
 * e a geração de horários disponíveis.
 */
@Service
public class HorarioTrabalhoService {

    // Adicionado um Logger para registrar informações importantes sobre a execução,
    // o que é extremamente útil para depurar e monitorar o comportamento do sistema.
    private static final Logger logger = LoggerFactory.getLogger(HorarioTrabalhoService.class);

    private final HorarioTrabalhoRepository horarioTrabalhoRepository;
    private final HorarioDisponivelRepository horarioDisponivelRepository;
    private final FuncionarioRepository funcionarioRepository;

    public HorarioTrabalhoService(HorarioTrabalhoRepository horarioTrabalhoRepository, HorarioDisponivelRepository horarioDisponivelRepository, FuncionarioRepository funcionarioRepository) {
        this.horarioTrabalhoRepository = horarioTrabalhoRepository;
        this.horarioDisponivelRepository = horarioDisponivelRepository;
        this.funcionarioRepository = funcionarioRepository;
    }

    /**
     * Adiciona um novo bloco de horário na grade semanal de um fisioterapeuta,
     * validando para evitar sobreposição de horários.
     * @param request DTO com os dados do novo horário.
     * @return um HorarioTrabalhoResponse com os dados do horário salvo.
     * @throws BusinessRuleException se o horário de fim for antes do início ou se houver conflito com um horário existente.
     */
    @Transactional
    public HorarioTrabalhoResponse adicionarHorario(HorarioTrabalhoRequest request) {
        // Validação da regra de negócio: hora de fim deve ser após a hora de início.
        if (!request.horaFim().isAfter(request.horaInicio())) {
            throw new BusinessRuleException("O horário de fim deve ser posterior ao horário de início.");
        }

        // Validação de conflito: busca horários existentes para o mesmo profissional e dia da semana.
        List<HorarioTrabalho> horariosExistentes = horarioTrabalhoRepository.findByFisioterapeuta_IdUsuarioAndDiaDaSemana(
                request.idFisioterapeuta(),
                request.diaDaSemana()
        );

        // Verifica se o novo horário se sobrepõe a algum dos horários já cadastrados.
        for (HorarioTrabalho existente : horariosExistentes) {
            boolean haConflito = request.horaInicio().isBefore(existente.getHoraFim()) &&
                    existente.getHoraInicio().isBefore(request.horaFim());
            if (haConflito) {
                throw new BusinessRuleException(
                        "Conflito de horário! O período de " + request.horaInicio() + " às " + request.horaFim() +
                                " se sobrepõe ao horário já cadastrado de " + existente.getHoraInicio() + " às " + existente.getHoraFim() + "."
                );
            }
        }

        // Se não houver conflitos, prossegue com a criação.
        var fisioterapeuta = funcionarioRepository.getReferenceById(request.idFisioterapeuta());
        var novoHorario = new HorarioTrabalho();
        novoHorario.setFisioterapeuta(fisioterapeuta);
        novoHorario.setDiaDaSemana(request.diaDaSemana());
        novoHorario.setHoraInicio(request.horaInicio());
        novoHorario.setHoraFim(request.horaFim());

        var horarioSalvo = horarioTrabalhoRepository.save(novoHorario);
        return new HorarioTrabalhoResponse(horarioSalvo);
    }

    /**
     * Gera todos os slots de horários disponíveis para um fisioterapeuta em um mês/ano específico.
     * Esta operação primeiro apaga os horários disponíveis existentes para o período e depois os recria
     * com base na grade de trabalho atual.
     * @param idFisioterapeuta ID do fisioterapeuta.
     * @param ano O ano para o qual a agenda será gerada.
     * @param mes O mês para o qual a agenda será gerada.
     * @throws BusinessRuleException se o fisioterapeuta não tiver uma grade de trabalho cadastrada.
     */
    @Transactional
    public void gerarDisponibilidadeParaMes(Integer idFisioterapeuta, int ano, int mes) {
        logger.info("Iniciando geração de agenda para Fisioterapeuta ID: {}, Mês/Ano: {}/{}", idFisioterapeuta, mes, ano);

        var fisioterapeuta = funcionarioRepository.findById(idFisioterapeuta)
                .orElseThrow(() -> new ResourceNotFoundException("Fisioterapeuta com ID " + idFisioterapeuta + " não encontrado."));

        YearMonth anoMes = YearMonth.of(ano, mes);
        LocalDate primeiroDiaDoMes = anoMes.atDay(1);
        LocalDate ultimoDiaDoMes = anoMes.atEndOfMonth();
        LocalDate hoje = LocalDate.now();

        // 1. Apaga apenas os horários FUTUROS para evitar perda de histórico
        LocalDate dataInicioDelecao = hoje.isAfter(primeiroDiaDoMes) ? hoje : primeiroDiaDoMes;
        horarioDisponivelRepository.deleteByFisioterapeutaIdUsuarioAndDataBetween(
                idFisioterapeuta, dataInicioDelecao, ultimoDiaDoMes
        );

        // 2. Busca a grade de trabalho
        List<HorarioTrabalho> gradeDeTrabalho = horarioTrabalhoRepository.findByFisioterapeutaIdUsuario(idFisioterapeuta)
                .stream()
                .filter(HorarioTrabalho::isAtivo)
                .toList();

        if (gradeDeTrabalho.isEmpty()) {
            logger.warn("Fisioterapeuta não possui grade de trabalho ativa.");
            throw new BusinessRuleException("O fisioterapeuta não possui uma grade de trabalho ativa definida.");
        }

        // 3. Gera apenas slots FUTUROS
        List<HorarioDisponivel> novosHorarios = new ArrayList<>();
        for (LocalDate dataAtual = primeiroDiaDoMes; !dataAtual.isAfter(ultimoDiaDoMes); dataAtual = dataAtual.plusDays(1)) {

            // Ignora dias passados
            if (dataAtual.isBefore(hoje)) continue;

            DayOfWeek diaDaSemanaAtual = dataAtual.getDayOfWeek();

            for (HorarioTrabalho template : gradeDeTrabalho) {
                if (template.getDiaDaSemana() == diaDaSemanaAtual) {
                    LocalTime slotInicio = template.getHoraInicio();
                    LocalTime agora = LocalTime.now();

                    while (slotInicio.isBefore(template.getHoraFim())) {
                        // Para o dia atual: ignora horários que já passaram
                        if (dataAtual.equals(hoje) && slotInicio.isBefore(agora)) {
                            slotInicio = slotInicio.plusHours(1);
                            continue;
                        }

                        var horarioDisponivel = new HorarioDisponivel();
                        horarioDisponivel.setFisioterapeuta(fisioterapeuta);
                        horarioDisponivel.setData(dataAtual);
                        horarioDisponivel.setHoraInicio(slotInicio);
                        horarioDisponivel.setHoraFim(slotInicio.plusHours(1));
                        horarioDisponivel.setDisponivel(true);

                        novosHorarios.add(horarioDisponivel);
                        slotInicio = slotInicio.plusHours(1);
                    }
                }
            }
        }

        horarioDisponivelRepository.saveAll(novosHorarios);
        logger.info("Geração finalizada. {} slots futuros criados.", novosHorarios.size());
    }

    @Transactional(readOnly = true)
    public List<HorarioTrabalhoResponse> listarHorariosPorFisioterapeuta(Integer idFisioterapeuta) {
        return horarioTrabalhoRepository.findByFisioterapeutaIdUsuario(idFisioterapeuta)
                .stream()
                .filter(HorarioTrabalho::isAtivo)
                .map(horario -> new HorarioTrabalhoResponse(horario))
                .collect(Collectors.toList());
    }

    @Transactional
    public void removerHorario(Integer idHorario) {
        // Valida se o horário existe antes de tentar deletá-lo.
        if (!horarioTrabalhoRepository.existsById(idHorario)) {
            throw new ResourceNotFoundException("Horário de trabalho com ID " + idHorario + " não encontrado.");
        }
        horarioTrabalhoRepository.deleteById(idHorario);
    }
}
