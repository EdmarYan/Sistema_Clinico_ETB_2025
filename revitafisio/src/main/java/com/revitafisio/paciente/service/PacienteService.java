package com.revitafisio.paciente.service;

import com.revitafisio.entities.usuarios.Contato;
import com.revitafisio.entities.usuarios.Paciente;
import com.revitafisio.entities.usuarios.Usuario;
import com.revitafisio.exception.BusinessRuleException;
import com.revitafisio.exception.ResourceNotFoundException;
import com.revitafisio.paciente.dto.AtualizarPacienteRequest;
import com.revitafisio.funcionario.dto.CriarContatoRequest;
import com.revitafisio.funcionario.dto.CriarPacienteRequest;
import com.revitafisio.paciente.dto.PacienteDetalhesResponse;
import com.revitafisio.paciente.dto.PacienteResponse;
import com.revitafisio.paciente.repository.PacienteRepository;
import com.revitafisio.entities.usuarios.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Serviço que encapsula a lógica de negócio para o gerenciamento de pacientes.
 */
@Service
public class PacienteService {

    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;

    public PacienteService(UsuarioRepository usuarioRepository, PacienteRepository pacienteRepository) {
        this.usuarioRepository = usuarioRepository;
        this.pacienteRepository = pacienteRepository;
    }

    /**
     * Cria um novo paciente no sistema, incluindo seus contatos iniciais.
     * @param request DTO com os dados do novo paciente.
     * @return O ID do paciente criado.
     * @throws BusinessRuleException se o CPF já estiver cadastrado.
     */
    @Transactional
    public Integer criarPaciente(CriarPacienteRequest request) {
        // Regra de Negócio: Valida a unicidade do CPF antes de prosseguir.
        if (usuarioRepository.findByCpf(request.cpf()).isPresent()) {
            throw new BusinessRuleException("CPF já cadastrado no sistema.");
        }

        var novoPaciente = new Paciente();
        novoPaciente.setNome(request.nome());
        novoPaciente.setCpf(request.cpf());
        novoPaciente.setDataNascimento(request.dataNascimento());
        novoPaciente.setAtivo(true);

        // Solução de Design: Como pacientes não fazem login, o campo 'senha' (que é obrigatório
        // na entidade Usuario) recebe um valor aleatório e único para satisfazer a restrição
        // do banco de dados, sem armazenar uma senha real ou nula.
        novoPaciente.setSenha(UUID.randomUUID().toString());

        // Processa e associa a lista de contatos, se houver.
        if (request.contatos() != null) {
            Set<Contato> contatos = request.contatos().stream()
                    .map(dto -> toContatoEntity(dto, novoPaciente))
                    .collect(Collectors.toSet());
            novoPaciente.setContatos(contatos);
        }

        Usuario pacienteSalvo = usuarioRepository.save(novoPaciente);
        return pacienteSalvo.getIdUsuario();
    }

    /**
     * Busca pacientes ativos cujo nome contenha o termo pesquisado.
     * @param nome O termo de busca.
     * @return Uma lista de DTOs com os dados resumidos dos pacientes encontrados.
     */
    @Transactional(readOnly = true)
    public List<PacienteResponse> buscarPorNome(String nome) {
        var pacientes = pacienteRepository.findByAtivoTrueAndNomeContainingIgnoreCase(nome);
        return pacientes.stream()
                .map(this::toPacienteResponse)
                .toList();
    }

    /**
     * Busca um único paciente ativo pelo seu CPF.
     * @param cpf O CPF a ser buscado.
     * @return Uma lista contendo o único paciente encontrado ou uma lista vazia.
     */
    @Transactional(readOnly = true)
    public List<PacienteResponse> buscarPorCpf(String cpf) {
        // Vínculo: Usa o novo método do PacienteRepository.
        return pacienteRepository.findByAtivoTrueAndCpf(cpf)
                .map(this::toPacienteResponse) // Mapeia o paciente para o DTO de resposta.
                .map(Collections::singletonList) // Converte o único resultado em uma lista.
                .orElse(Collections.emptyList()); // Se não encontrar, retorna uma lista vazia.
    }

    /**
     * Busca os detalhes completos de um paciente pelo seu ID.
     * @param id O ID do paciente.
     * @return Um DTO com os detalhes do paciente.
     * @throws ResourceNotFoundException se o paciente não for encontrado.
     */
    @Transactional(readOnly = true)
    public PacienteDetalhesResponse buscarPorId(Integer id) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente "+id+" não encontrado"));
        return PacienteDetalhesResponse.fromEntity(paciente);
    }

    /**
     * Busca todos os pacientes que estão com o status INATIVO.
     * @return Uma lista de DTOs com os dados resumidos dos pacientes inativos.
     */
    @Transactional(readOnly = true)
    public List<PacienteResponse> buscarTodosInativos() {
        return pacienteRepository.findAllByAtivoFalse()
                .stream()
                .map(this::toPacienteResponse)
                .toList();
    }

    /**
     * Atualiza os dados cadastrais de um paciente.
     * @param id O ID do paciente a ser atualizado.
     * @param request DTO com os novos dados.
     * @return Um DTO com os dados atualizados do paciente.
     * @throws ResourceNotFoundException se o paciente não for encontrado.
     * @throws BusinessRuleException se o paciente a ser atualizado estiver inativo.
     */
    @Transactional
    public PacienteDetalhesResponse atualizarPaciente(Integer id, AtualizarPacienteRequest request) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente "+id+" não encontrado"));

        if (!paciente.isAtivo()) {
            throw new BusinessRuleException("Paciente inativo não pode ser atualizado");
        }

        paciente.setNome(request.nome());
        paciente.setDataNascimento(request.dataNascimento());

        Usuario pacienteSalvo = usuarioRepository.save(paciente);
        return PacienteDetalhesResponse.fromEntity(pacienteSalvo);
    }

    /**
     * Inativa o cadastro de um paciente.
     * @param id O ID do paciente a ser inativado.
     * @throws ResourceNotFoundException se o paciente não for encontrado.
     */
    @Transactional
    public void inativarPaciente(Integer id) {
        var paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente com ID " + id + " não encontrado."));
        paciente.setAtivo(false);
        usuarioRepository.save(paciente);
    }

    /**
     * Reativa o cadastro de um paciente.
     * @param id O ID do paciente a ser reativado.
     * @throws ResourceNotFoundException se o paciente não for encontrado.
     */
    @Transactional
    public void ativarPaciente(Integer id) {
        var paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente com ID " + id + " não encontrado."));
        paciente.setAtivo(true);
        usuarioRepository.save(paciente);
    }

    /**
     * Busca todos os pacientes que estão com o status ATIVO.
     * @return Uma lista de DTOs com os dados resumidos de todos os pacientes ativos.
     */
    @Transactional(readOnly = true)
    public List<PacienteResponse> buscarTodos() {
        return pacienteRepository.findAllByAtivoTrue()
                .stream()
                .map(this::toPacienteResponse)
                .toList();
    }

    // --- MÉTODOS AUXILIARES DE MAPEAMENTO (Mappers) ---

    /**
     * Converte um DTO de criação de contato para a entidade Contato.
     * @param dto O DTO {@link CriarContatoRequest} com os dados do contato.
     * @param paciente A entidade Paciente à qual o contato será associado.
     * @return A entidade {@link Contato} pronta para ser persistida.
     */
    private Contato toContatoEntity(CriarContatoRequest dto, Paciente paciente) {
        Contato contato = new Contato();
        contato.setUsuario(paciente);
        contato.setTipo(dto.tipo());
        contato.setValor(dto.valor());
        contato.setPrincipal(dto.principal());
        return contato;
    }

    /**
     * Converte uma entidade Paciente para um DTO de resposta resumido.
     * @param paciente A entidade a ser convertida.
     * @return O DTO {@link PacienteResponse} para uso em listas.
     */
    private PacienteResponse toPacienteResponse(Paciente paciente) {
        return new PacienteResponse(
                paciente.getIdUsuario(),
                paciente.getNome(),
                paciente.getCpf()
        );
    }
}
