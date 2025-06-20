package com.revitafisio.paciente.service;

import com.revitafisio.entities.paciente.AvaliacaoOrtopedia;
import com.revitafisio.entities.usuarios.Paciente;
import com.revitafisio.exception.BusinessRuleException;
import com.revitafisio.exception.ResourceNotFoundException;
import com.revitafisio.paciente.dto.AvaliacaoOrtopediaRequest;
import com.revitafisio.paciente.repository.AvaliacaoOrtopediaRepository;
import com.revitafisio.funcionario.repository.FuncionarioRepository;
import com.revitafisio.paciente.repository.PacienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Serviço que encapsula a lógica de negócio para a gestão das
 * fichas de Avaliação de Ortopedia.
 */
@Service
public class AvaliacaoOrtopediaService {

    private final AvaliacaoOrtopediaRepository avaliacaoRepository;
    private final PacienteRepository pacienteRepository;
    private final FuncionarioRepository funcionarioRepository;

    public AvaliacaoOrtopediaService(AvaliacaoOrtopediaRepository avaliacaoRepository, PacienteRepository pacienteRepository, FuncionarioRepository funcionarioRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.pacienteRepository = pacienteRepository;
        this.funcionarioRepository = funcionarioRepository;
    }

    /**
     * Salva (cria ou atualiza) uma avaliação de ortopedia para um paciente.
     * Este metodo implementa uma lógica "upsert": se uma avaliação para o paciente
     * já existir, ela será atualizada; caso contrário, uma nova será criada.
     *
     * @param request DTO com os dados da avaliação.
     * @return A entidade AvaliacaoOrtopedia salva.
     * @throws ResourceNotFoundException se o paciente ou fisioterapeuta não forem encontrados.
     * @throws BusinessRuleException se o paciente estiver inativo ou se campos obrigatórios estiverem em branco.
     */
    @Transactional
    public AvaliacaoOrtopedia salvar(AvaliacaoOrtopediaRequest request) {
        // 1. Validação do Paciente.
        // Vínculo: Utiliza o PacienteRepository para buscar o paciente.
        Paciente paciente = pacienteRepository.findById(request.idPaciente())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente com ID " + request.idPaciente() + " não encontrado."));

        // Regra de Negócio: Impede operações em pacientes inativos.
        if (!paciente.isAtivo()) {
            throw new BusinessRuleException("Não é possível salvar avaliação para um paciente inativo.");
        }

        // 2. Validação do Fisioterapeuta.
        // Vínculo: Utiliza o FuncionarioRepository para buscar o fisioterapeuta.
        var fisioterapeuta = funcionarioRepository.findById(request.idFisioterapeuta())
                .orElseThrow(() -> new ResourceNotFoundException("Fisioterapeuta com ID " + request.idFisioterapeuta() + " não encontrado."));

        // 3. Validação de Campos Essenciais da Avaliação.
        if (request.queixa_principal() == null || request.queixa_principal().isBlank()) {
            throw new BusinessRuleException("O campo 'Queixa Principal' é obrigatório.");
        }
        if (request.diagnostico_fisioterapeutico() == null || request.diagnostico_fisioterapeutico().isBlank()) {
            throw new BusinessRuleException("O campo 'Diagnóstico Fisioterapêutico' é obrigatório.");
        }

        // 4. Lógica de "Criar ou Atualizar" (Upsert).
        // Busca uma avaliação existente para o paciente. Se não encontrar, cria uma nova instância.
        AvaliacaoOrtopedia avaliacao = avaliacaoRepository.findByPacienteIdUsuario(request.idPaciente())
                .orElse(new AvaliacaoOrtopedia());

        // 5. Mapeamento completo dos dados do DTO para a Entidade.
        avaliacao.setPaciente(paciente);
        avaliacao.setFisioterapeuta(fisioterapeuta);
        // Garante que a data da avaliação seja definida apenas na criação.
        if (avaliacao.getDataAvaliacao() == null) {
            avaliacao.setDataAvaliacao(LocalDate.now());
        }
        avaliacao.setProfissao(request.profissao());
        avaliacao.setPressao_arterial(request.pressao_arterial());
        avaliacao.setAvaliacao_postural(request.avaliacao_postural());
        avaliacao.setAlergias(request.alergias());
        avaliacao.setIndicacao_medica(request.indicacao_medica());
        avaliacao.setQueixa_principal(request.queixa_principal());
        avaliacao.setHda_hdp(request.hda_hdp());
        avaliacao.setDoencas_cardiacas(request.doencas_cardiacas());
        avaliacao.setComorbidades(request.comorbidades());
        avaliacao.setMedicacoes(request.medicacoes());
        avaliacao.setDiagnostico_fisioterapeutico(request.diagnostico_fisioterapeutico());
        avaliacao.setObjetivos(request.objetivos());
        avaliacao.setConduta(request.conduta());
        avaliacao.setObservacoes(request.observacoes());
        avaliacao.setFrequencia_cardiaca(request.frequencia_cardiaca());
        avaliacao.setFrequencia_respiratoria(request.frequencia_respiratoria());
        avaliacao.setTemperatura(request.temperatura());

        // 6. Persiste a entidade no banco de dados e a retorna.
        return avaliacaoRepository.save(avaliacao);
    }

    /**
     * Busca a avaliação de ortopedia de um paciente específico pelo ID do paciente.
     * @param idPaciente O ID do paciente.
     * @return um Optional contendo a AvaliacaoOrtopedia se encontrada, ou um Optional vazio.
     */
    @Transactional(readOnly = true)
    public Optional<AvaliacaoOrtopedia> buscarPorPaciente(Integer idPaciente) {
        // Vínculo: Chama o metodo de busca customizado definido no AvaliacaoOrtopediaRepository.
        return avaliacaoRepository.findByPacienteIdUsuario(idPaciente);
    }
}
