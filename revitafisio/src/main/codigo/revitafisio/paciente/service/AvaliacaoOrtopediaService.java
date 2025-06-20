package com.revitafisio.paciente.service;

import com.revitafisio.entities.paciente.AvaliacaoOrtopedia;
import com.revitafisio.entities.usuarios.Paciente;
import com.revitafisio.paciente.dto.AvaliacaoOrtopediaRequest;
import com.revitafisio.paciente.repository.AvaliacaoOrtopediaRepository;
import com.revitafisio.funcionario.repository.FuncionarioRepository;
import com.revitafisio.paciente.repository.PacienteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

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

    @Transactional
    public AvaliacaoOrtopedia salvar(AvaliacaoOrtopediaRequest request) {
        // Validação do paciente
        Paciente paciente = pacienteRepository.findById(request.idPaciente())
                .orElseThrow(() -> new EntityNotFoundException("Paciente não encontrado."));

        if (!paciente.isAtivo()) {
            throw new IllegalStateException("Não é possível salvar avaliação para um paciente inativo.");
        }

        // Validação do fisioterapeuta
        var fisioterapeuta = funcionarioRepository.findById(request.idFisioterapeuta())
                .orElseThrow(() -> new EntityNotFoundException("Fisioterapeuta não encontrado."));

        // Validação de campos obrigatórios
        if (request.queixa_principal() == null || request.queixa_principal().isBlank()) {
            throw new IllegalArgumentException("Queixa principal é obrigatória.");
        }
        if (request.diagnostico_fisioterapeutico() == null || request.diagnostico_fisioterapeutico().isBlank()) {
            throw new IllegalArgumentException("Diagnóstico fisioterapêutico é obrigatório.");
        }

        AvaliacaoOrtopedia avaliacao = avaliacaoRepository.findByPacienteIdUsuario(request.idPaciente())
                .orElse(new AvaliacaoOrtopedia());

        // Mapeamento completo dos campos
        avaliacao.setPaciente(paciente);
        avaliacao.setFisioterapeuta(fisioterapeuta);
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

        return avaliacaoRepository.save(avaliacao);
    }

    @Transactional(readOnly = true)
    public Optional<AvaliacaoOrtopedia> buscarPorPaciente(Integer idPaciente) {
        return avaliacaoRepository.findByPacienteIdUsuario(idPaciente);
    }
}