package com.revitafisio.agendamento.service;

import com.revitafisio.agendamento.dto.HorarioDisponivelResponse;
import com.revitafisio.agendamento.repository.HorarioDisponivelRepository;
import com.revitafisio.exception.BusinessRuleException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço responsável pela lógica de negócio relacionada aos horários disponíveis.
 * Sua principal função é buscar e fornecer os slots de horários vagos para agendamento.
 *
 * Anotação:
 * @Service: Marca esta classe como um componente de serviço do Spring,
 * contendo a lógica de negócio e sendo elegível para injeção de dependência.
 */
@Service
public class HorarioDisponivelService {

    private final HorarioDisponivelRepository horarioDisponivelRepository;

    /**
     * Injeção de dependência do repositório via construtor.
     * @param horarioDisponivelRepository O repositório para acesso aos dados de HorarioDisponivel.
     */
    public HorarioDisponivelService(HorarioDisponivelRepository horarioDisponivelRepository) {
        this.horarioDisponivelRepository = horarioDisponivelRepository;
    }

    /**
     * Busca todos os horários disponíveis para um fisioterapeuta específico dentro de um período de datas.
     *
     * @param idFisioterapeuta O ID do fisioterapeuta a ser consultado.
     * @param inicio A data de início do período de busca.
     * @param fim A data de fim do período de busca.
     * @return Uma lista de {@link HorarioDisponivelResponse}, que é o DTO para ser enviado ao frontend.
     * @throws BusinessRuleException se a data de início for posterior à data de fim.
     */
    @Transactional(readOnly = true) // Otimização para consultas que não alteram o banco de dados.
    public List<HorarioDisponivelResponse> buscarPorFisioEPeriodo(Integer idFisioterapeuta, LocalDate inicio, LocalDate fim) {
        // Validação da regra de negócio: a data de início não pode ser posterior à data de fim.
        if (inicio.isAfter(fim)) {
            throw new BusinessRuleException("A data de início não pode ser posterior à data de término do período.");
        }

        // Vínculo: Chama o metodo de busca customizado definido na interface HorarioDisponivelRepository.
        // O Spring Data JPA cria a query automaticamente baseada no nome do método.
        return horarioDisponivelRepository.findByFisioterapeutaIdUsuarioAndDataBetween(idFisioterapeuta, inicio, fim)
                .stream()
                // Mapeia cada entidade 'HorarioDisponivel' para o seu DTO de resposta 'HorarioDisponivelResponse'.
                .map(HorarioDisponivelResponse::new)
                .collect(Collectors.toList());
    }
}
