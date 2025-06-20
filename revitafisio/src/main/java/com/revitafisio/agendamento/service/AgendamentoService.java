package com.revitafisio.agendamento.service;

import com.revitafisio.agendamento.dto.AgendamentoResponse;
import com.revitafisio.agendamento.dto.CriarAgendamentoRequest;
import com.revitafisio.agendamento.repository.AgendamentoRepository;
import com.revitafisio.agendamento.repository.HorarioDisponivelRepository;
import com.revitafisio.entities.agendamentos.Agendamento;
import com.revitafisio.entities.agendamentos.HorarioDisponivel;
import com.revitafisio.entities.usuarios.repository.UsuarioRepository;
import com.revitafisio.exception.BusinessRuleException;
import com.revitafisio.exception.ResourceNotFoundException;
import com.revitafisio.funcionario.repository.EspecialidadeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço que encapsula toda a lógica de negócio para o gerenciamento de agendamentos.
 *
 * Anotações:
 * @Service: Marca esta classe como um componente de serviço do Spring. É aqui que
 * a lógica de negócio principal deve residir, mantendo os controllers "leves".
 */
@Service
public class AgendamentoService {

    // Injeção de todas as dependências de repositório necessárias para o serviço.
    private final AgendamentoRepository agendamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EspecialidadeRepository especialidadeRepository;
    private final HorarioDisponivelRepository horarioDisponivelRepository;

    public AgendamentoService(AgendamentoRepository agendamentoRepository, UsuarioRepository usuarioRepository, EspecialidadeRepository especialidadeRepository, HorarioDisponivelRepository horarioDisponivelRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.especialidadeRepository = especialidadeRepository;
        this.horarioDisponivelRepository = horarioDisponivelRepository;
    }

    /**
     * Cria um novo agendamento, aplicando as regras de negócio de disponibilidade.
     *
     * @param request DTO com os dados do agendamento a ser criado.
     * @return um AgendamentoResponse com os dados do agendamento salvo.
     * @throws BusinessRuleException se o horário solicitado não estiver disponível.
     * @throws ResourceNotFoundException se o paciente, fisioterapeuta ou especialidade não forem encontrados.
     */
    @Transactional // Garante que toda a operação seja atômica (ou tudo funciona, ou nada é salvo).
    public AgendamentoResponse criarAgendamento(CriarAgendamentoRequest request) {
        // 1. Busca o slot de horário disponível correspondente ao início do agendamento.
        // Vínculo: O metodo findByFisioterapeuta... foi definido em HorarioDisponivelRepository.
        var slotDisponivel = horarioDisponivelRepository
                .findByFisioterapeutaIdUsuarioAndDataAndHoraInicio(
                        request.idFisioterapeuta(),
                        request.dataHoraInicio().toLocalDate(),
                        request.dataHoraInicio().toLocalTime()
                )
                // Pega o primeiro slot encontrado que esteja marcado como 'disponivel'.
                .stream()
                .filter(HorarioDisponivel::isDisponivel)
                .findFirst()
                // Se nenhum slot for encontrado, lança nossa exceção de regra de negócio.
                .orElseThrow(() -> new BusinessRuleException("Horário não está disponível ou não existe."));

        // 2. Valida se o paciente, fisioterapeuta e especialidade existem antes de criar o agendamento.
        // O uso de `existsById` é mais performático do que carregar a entidade inteira.
        if (!usuarioRepository.existsById(request.idPaciente())) {
            throw new ResourceNotFoundException("Paciente com ID " + request.idPaciente() + " não encontrado.");
        }
        if (!usuarioRepository.existsById(request.idFisioterapeuta())) {
            throw new ResourceNotFoundException("Fisioterapeuta com ID " + request.idFisioterapeuta() + " não encontrado.");
        }
        if (!especialidadeRepository.existsById(request.idEspecialidade())) {
            throw new ResourceNotFoundException("Especialidade com ID " + request.idEspecialidade() + " não encontrada.");
        }

        // 3. Marca o slot de horário como ocupado e salva a alteração.
        slotDisponivel.setDisponivel(false);
        horarioDisponivelRepository.save(slotDisponivel);

        // 4. Cria a nova entidade Agendamento com os dados da requisição.
        var novoAgendamento = new Agendamento();
        novoAgendamento.setPaciente(usuarioRepository.getReferenceById(request.idPaciente()));
        novoAgendamento.setFisioterapeuta(usuarioRepository.getReferenceById(request.idFisioterapeuta()));
        novoAgendamento.setEspecialidade(especialidadeRepository.getReferenceById(request.idEspecialidade()));
        novoAgendamento.setDataHoraInicio(request.dataHoraInicio());
        novoAgendamento.setDataHoraFim(request.dataHoraFim());
        novoAgendamento.setStatus(Agendamento.StatusAgendamento.CONFIRMADO); // Status inicial padrão

        // 5. Salva o novo agendamento e retorna um DTO de resposta.
        var agendamentoSalvo = agendamentoRepository.save(novoAgendamento);
        // CORREÇÃO: Usando o construtor explícito para garantir a compatibilidade de tipos.
        return new AgendamentoResponse(agendamentoSalvo);
    }

    /**
     * Busca todos os agendamentos de um fisioterapeuta dentro de um período específico.
     *
     * @return Uma lista de AgendamentoResponse.
     */
    @Transactional(readOnly = true) // Otimização para consultas que não modificam dados.
    public List<AgendamentoResponse> buscarAgenda(Integer idFisioterapeuta, LocalDateTime inicio, LocalDateTime fim) {
        // CORREÇÃO: O nome do metodo foi corrigido para corresponder ao definido no AgendamentoRepository.
        return agendamentoRepository
                .findByFisioterapeuta_IdUsuarioAndDataHoraInicioGreaterThanEqualAndDataHoraFimLessThanEqual(idFisioterapeuta, inicio, fim)
                .stream()
                // CORREÇÃO: Usando o construtor explícito para evitar problemas com o 'method reference' e tipos.
                .map(agendamento -> new AgendamentoResponse(agendamento))
                .collect(Collectors.toList());
    }

    /**
     * Busca agendamentos que já passaram da hora e ainda estão com status 'CONFIRMADO',
     * indicando que precisam de uma ação (ser marcados como 'REALIZADO' ou 'NAO_COMPARECEU').
     *
     * @return Uma lista de agendamentos pendentes.
     */
    @Transactional(readOnly = true)
    public List<AgendamentoResponse> buscarAgendamentosPendentesDeStatus() {
        return agendamentoRepository.findAllByStatusAndDataHoraInicioBefore(
                        Agendamento.StatusAgendamento.CONFIRMADO, LocalDateTime.now())
                .stream()
                .map(AgendamentoResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Atualiza o status de um agendamento. Se o novo status for 'CANCELADO',
     * o horário correspondente é liberado.
     *
     * @param agendamentoId ID do agendamento a ser atualizado.
     * @param novoStatusStr O novo status como uma String.
     */
    @Transactional
    public void atualizarStatus(Long agendamentoId, String novoStatusStr) {
        // Busca o agendamento ou lança uma exceção se não for encontrado.
        var agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento com ID " + agendamentoId + " não encontrado."));

        Agendamento.StatusAgendamento novoStatus;
        try {
            // Converte a string do novo status para o enum, garantindo que seja um valor válido.
            novoStatus = Agendamento.StatusAgendamento.valueOf(novoStatusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Tratamento de erro: se o status enviado pelo frontend for inválido.
            throw new BusinessRuleException("Status '" + novoStatusStr + "' é inválido.");
        }

        // Regra de Negócio: Se o agendamento for cancelado, libera o horário.
        if (novoStatus == Agendamento.StatusAgendamento.CANCELADO && agendamento.getStatus() != Agendamento.StatusAgendamento.CANCELADO) {
            liberarHorario(agendamento);
        }

        agendamento.setStatus(novoStatus);
        agendamentoRepository.save(agendamento);
    }

    /**
     * Metodo auxiliar privado para liberar um slot de horário quando um agendamento é cancelado.
     * @param agendamento O agendamento que foi cancelado.
     */
    private void liberarHorario(Agendamento agendamento) {
        var slots = horarioDisponivelRepository.findByFisioterapeutaIdUsuarioAndDataAndHoraInicio(
                agendamento.getFisioterapeuta().getIdUsuario(),
                agendamento.getDataHoraInicio().toLocalDate(),
                agendamento.getDataHoraInicio().toLocalTime()
        );
        // Itera sobre os slots encontrados e os marca como disponíveis.
        slots.forEach(slot -> slot.setDisponivel(true));

        // Salva todos os slots modificados de uma vez.
        if (!slots.isEmpty()) {
            horarioDisponivelRepository.saveAll(slots);
        }
    }
}
