package com.revitafisio.paciente.service;

import com.revitafisio.entities.paciente.Evolucao;
import com.revitafisio.entities.usuarios.Paciente;
import com.revitafisio.funcionario.dto.CriarEvolucaoRequest;
import com.revitafisio.paciente.dto.EvolucaoResponse;
import com.revitafisio.paciente.repository.EvolucaoRepository;
import com.revitafisio.funcionario.repository.FuncionarioRepository;
import com.revitafisio.paciente.repository.PacienteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EvolucaoService {

    private final EvolucaoRepository evolucaoRepository;
    private final PacienteRepository pacienteRepository;
    private final FuncionarioRepository funcionarioRepository;

    public EvolucaoService(EvolucaoRepository evolucaoRepository, PacienteRepository pacienteRepository, FuncionarioRepository funcionarioRepository) {
        this.evolucaoRepository = evolucaoRepository;
        this.pacienteRepository = pacienteRepository;
        this.funcionarioRepository = funcionarioRepository;
    }

    @Transactional
    public EvolucaoResponse salvarEvolucao(CriarEvolucaoRequest request) {
        // Validação do paciente
        Paciente paciente = pacienteRepository.findById(request.idPaciente())
                .orElseThrow(() -> new EntityNotFoundException("Paciente não encontrado."));

        if (!paciente.isAtivo()) {
            throw new IllegalStateException("Não é possível salvar evolução para um paciente inativo.");
        }

        // Validação do fisioterapeuta
        var fisioterapeuta = funcionarioRepository.findById(request.idFisioterapeuta())
                .orElseThrow(() -> new EntityNotFoundException("Fisioterapeuta não encontrado."));

        // Validação de campo obrigatório
        if (request.descricao() == null || request.descricao().isBlank()) {
            throw new IllegalArgumentException("Descrição da evolução é obrigatória.");
        }

        var evolucao = new Evolucao();
        evolucao.setPaciente(paciente);
        evolucao.setFisioterapeuta(fisioterapeuta);
        evolucao.setDescricao(request.descricao());
        evolucao.setData(LocalDate.now());
        evolucao.setPreenchida(true);

        var evolucaoSalva = evolucaoRepository.save(evolucao);

        return new EvolucaoResponse(evolucaoSalva);
    }

    @Transactional(readOnly = true)
    public List<EvolucaoResponse> listarEvolucoesPorPaciente(Integer idPaciente) {
        return evolucaoRepository.findByPacienteIdUsuarioOrderByDataDesc(idPaciente)
                .stream()
                .map(EvolucaoResponse::new)
                .collect(Collectors.toList());
    }
}