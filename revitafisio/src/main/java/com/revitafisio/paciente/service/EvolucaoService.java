package com.revitafisio.paciente.service;

import com.revitafisio.entities.paciente.Evolucao;
import com.revitafisio.entities.usuarios.Paciente;
import com.revitafisio.exception.BusinessRuleException;
import com.revitafisio.exception.ResourceNotFoundException;
import com.revitafisio.funcionario.dto.CriarEvolucaoRequest;
import com.revitafisio.paciente.dto.EvolucaoResponse;
import com.revitafisio.paciente.repository.EvolucaoRepository;
import com.revitafisio.funcionario.repository.FuncionarioRepository;
import com.revitafisio.paciente.repository.PacienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço que encapsula a lógica de negócio para o gerenciamento
 * dos registros de evolução do prontuário do paciente.
 */
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

    /**
     * Salva um novo registro de evolução para um paciente.
     * Diferente das avaliações, cada chamada a este metodo cria um novo registro,
     * mantendo um histórico contínuo.
     *
     * @param request DTO com os dados da nova evolução.
     * @return um EvolucaoResponse com os dados da evolução salva.
     * @throws ResourceNotFoundException se o paciente ou fisioterapeuta não forem encontrados.
     * @throws BusinessRuleException se o paciente estiver inativo ou a descrição estiver em branco.
     */
    @Transactional
    public EvolucaoResponse salvarEvolucao(CriarEvolucaoRequest request) {
        // 1. Valida a existência e o status do Paciente.
        Paciente paciente = pacienteRepository.findById(request.idPaciente())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente com ID " + request.idPaciente() + " não encontrado."));

        if (!paciente.isAtivo()) {
            throw new BusinessRuleException("Não é possível salvar evolução para um paciente inativo.");
        }

        // 2. Valida a existência do Fisioterapeuta.
        var fisioterapeuta = funcionarioRepository.findById(request.idFisioterapeuta())
                .orElseThrow(() -> new ResourceNotFoundException("Fisioterapeuta com ID " + request.idFisioterapeuta() + " não encontrado."));

        // 3. Valida a descrição da evolução.
        if (request.descricao() == null || request.descricao().isBlank()) {
            throw new BusinessRuleException("A descrição da evolução é obrigatória.");
        }

        // 4. Cria e preenche a nova entidade Evolucao.
        var evolucao = new Evolucao();
        evolucao.setPaciente(paciente);
        evolucao.setFisioterapeuta(fisioterapeuta);
        evolucao.setDescricao(request.descricao());
        evolucao.setData(LocalDate.now()); // A data é sempre a do momento do registro.
        evolucao.setPreenchida(true); // Marca como preenchida.

        // 5. Salva a nova evolução no banco de dados.
        var evolucaoSalva = evolucaoRepository.save(evolucao);

        // 6. Retorna um DTO com os dados da evolução salva.
        return new EvolucaoResponse(evolucaoSalva);
    }

    /**
     * Lista todo o histórico de evoluções de um paciente, ordenado pela data mais recente primeiro.
     * @param idPaciente O ID do paciente.
     * @return Uma lista de DTOs de evolução.
     */
    @Transactional(readOnly = true)
    public List<EvolucaoResponse> listarEvolucoesPorPaciente(Integer idPaciente) {
        // Vínculo: Chama o metodo de busca customizado definido no EvolucaoRepository.
        return evolucaoRepository.findByPacienteIdUsuarioOrderByDataDesc(idPaciente)
                .stream()
                .map(EvolucaoResponse::new)
                .collect(Collectors.toList());
    }
}
