package com.revitafisio.paciente.service;

import com.revitafisio.entities.paciente.AvaliacaoRpg;
import com.revitafisio.entities.usuarios.Paciente;
import com.revitafisio.exception.BusinessRuleException;
import com.revitafisio.exception.ResourceNotFoundException;
import com.revitafisio.paciente.dto.AvaliacaoRpgRequest;
import com.revitafisio.paciente.repository.AvaliacaoRpgRepository;
import com.revitafisio.funcionario.repository.FuncionarioRepository;
import com.revitafisio.paciente.repository.PacienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Serviço que encapsula a lógica de negócio para a gestão das
 * fichas de Avaliação de RPG (Reeducação Postural Global).
 */
@Service
public class AvaliacaoRpgService {

    private final AvaliacaoRpgRepository avaliacaoRepository;
    private final PacienteRepository pacienteRepository;
    private final FuncionarioRepository funcionarioRepository;

    public AvaliacaoRpgService(AvaliacaoRpgRepository avaliacaoRepository, PacienteRepository pacienteRepository, FuncionarioRepository funcionarioRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.pacienteRepository = pacienteRepository;
        this.funcionarioRepository = funcionarioRepository;
    }

    /**
     * Salva (cria ou atualiza) uma avaliação de RPG para um paciente.
     * Implementa uma lógica "upsert": se uma avaliação para o paciente já existir,
     * ela será atualizada; caso contrário, uma nova será criada.
     *
     * @param request DTO com os dados da avaliação.
     * @return A entidade AvaliacaoRpg salva.
     * @throws ResourceNotFoundException se o paciente ou fisioterapeuta não forem encontrados.
     * @throws BusinessRuleException se o paciente estiver inativo ou se campos obrigatórios estiverem em branco.
     */
    @Transactional
    public AvaliacaoRpg salvar(AvaliacaoRpgRequest request) {
        // 1. Valida a existência e o status do Paciente.
        Paciente paciente = pacienteRepository.findById(request.idPaciente())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente com ID " + request.idPaciente() + " não encontrado."));

        if (!paciente.isAtivo()) {
            throw new BusinessRuleException("Não é possível salvar avaliação para um paciente inativo.");
        }

        // 2. Valida a existência do Fisioterapeuta.
        var fisioterapeuta = funcionarioRepository.findById(request.idFisioterapeuta())
                .orElseThrow(() -> new ResourceNotFoundException("Fisioterapeuta com ID " + request.idFisioterapeuta() + " não encontrado."));

        // 3. Valida campos de negócio obrigatórios.
        if (request.diagnostico_clinico() == null || request.diagnostico_clinico().isBlank()) {
            throw new BusinessRuleException("O campo 'Diagnóstico Clínico' é obrigatório.");
        }
        if (request.tratamento_proposto() == null || request.tratamento_proposto().isBlank()) {
            throw new BusinessRuleException("O campo 'Tratamento Proposto' é obrigatório.");
        }

        // 4. Lógica de "Criar ou Atualizar" (Upsert).
        AvaliacaoRpg avaliacao = avaliacaoRepository.findByPacienteIdUsuario(request.idPaciente())
                .orElse(new AvaliacaoRpg());

        // 5. Mapeamento dos dados do DTO para a Entidade.
        avaliacao.setPaciente(paciente);
        avaliacao.setFisioterapeuta(fisioterapeuta);
        if (avaliacao.getDataAvaliacao() == null) {
            avaliacao.setDataAvaliacao(LocalDate.now());
        }

        // Campos de Texto
        avaliacao.setDiagnostico_clinico(request.diagnostico_clinico());
        avaliacao.setHma(request.hma());
        avaliacao.setPosicao_dor(request.posicao_dor());
        avaliacao.setOutras_patologias(request.outras_patologias());
        avaliacao.setOutros_exames(request.outros_exames());
        avaliacao.setMedicamentos_descricao(request.medicamentos_descricao());
        avaliacao.setOutros_desequilibrios(request.outros_desequilibrios());
        avaliacao.setTratamento_proposto(request.tratamento_proposto());
        avaliacao.setObservacoes(request.observacoes());

        // Campos Booleanos
        avaliacao.setRessonancia_magnetica(request.ressonancia_magnetica());
        avaliacao.setRaio_x(request.raio_x());
        avaliacao.setTomografia(request.tomografia());
        avaliacao.setUso_medicamentos(request.uso_medicamentos());

        // Campos Enum
        avaliacao.setGrau_dor(request.grau_dor());
        avaliacao.setCabeca(request.cabeca());
        avaliacao.setOmbros(request.ombros());
        avaliacao.setMaos(request.maos());
        avaliacao.setEias(request.eias());
        avaliacao.setJoelhos(request.joelhos());
        avaliacao.setLombar(request.lombar());
        avaliacao.setPelve(request.pelve());
        avaliacao.setEscapulas(request.escapulas());

        // 6. Persiste a entidade e a retorna.
        return avaliacaoRepository.save(avaliacao);
    }

    /**
     * Busca a avaliação de RPG de um paciente específico pelo ID do paciente.
     * @param idPaciente O ID do paciente.
     * @return um Optional contendo a AvaliacaoRpg se encontrada, ou um Optional vazio.
     */
    @Transactional(readOnly = true)
    public Optional<AvaliacaoRpg> buscarPorPaciente(Integer idPaciente) {
        return avaliacaoRepository.findByPacienteIdUsuario(idPaciente);
    }
}
