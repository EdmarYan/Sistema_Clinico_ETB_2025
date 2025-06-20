package com.revitafisio.funcionario.service;

import com.revitafisio.entities.usuarios.Admin;
import com.revitafisio.entities.usuarios.Fisioterapeuta;
import com.revitafisio.entities.usuarios.Recepcionista;
import com.revitafisio.entities.usuarios.Usuario;
import com.revitafisio.entities.usuarios.repository.UsuarioRepository;
import com.revitafisio.exception.BusinessRuleException;
import com.revitafisio.exception.ResourceNotFoundException;
import com.revitafisio.funcionario.dto.AtualizarFuncionarioRequest;
import com.revitafisio.funcionario.dto.CriarFuncionarioRequest;
import com.revitafisio.funcionario.dto.FuncionarioDetalhesResponse;
import com.revitafisio.funcionario.dto.FuncionarioResponse;
import com.revitafisio.funcionario.repository.EspecialidadeRepository;
import com.revitafisio.funcionario.repository.FuncionarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço que encapsula a lógica de negócio para o gerenciamento de funcionários.
 */
@Service
public class FuncionarioService {

    private final UsuarioRepository usuarioRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final EspecialidadeRepository especialidadeRepository;

    public FuncionarioService(UsuarioRepository usuarioRepository,
                              FuncionarioRepository funcionarioRepository,
                              EspecialidadeRepository especialidadeRepository) {
        this.usuarioRepository = usuarioRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.especialidadeRepository = especialidadeRepository;
    }

    /**
     * Cria um novo funcionário (Fisioterapeuta, Recepcionista ou Admin).
     * @param request DTO com os dados do novo funcionário.
     * @return O ID do funcionário criado.
     * @throws BusinessRuleException se o CPF já estiver cadastrado ou o tipo de funcionário for inválido.
     */
    @Transactional
    public Integer criarFuncionario(CriarFuncionarioRequest request) {
        // Validação da Regra de Negócio: Impede o cadastro de CPFs duplicados.
        if (usuarioRepository.findByCpf(request.cpf()).isPresent()) {
            throw new BusinessRuleException("CPF já cadastrado no sistema.");
        }

        // Utiliza um 'switch' para instanciar a classe correta com base no tipo informado.
        // Isso é crucial para a estratégia de herança SINGLE_TABLE do JPA.
        Usuario novoFuncionario;
        switch (request.tipo()) {
            case FISIOTERAPEUTA -> novoFuncionario = new Fisioterapeuta();
            case RECEPCIONISTA -> novoFuncionario = new Recepcionista();
            case ADMIN -> novoFuncionario = new Admin();
            default -> throw new BusinessRuleException("Tipo de funcionário inválido: " + request.tipo());
        }

        // Preenche os dados herdados da classe Usuario.
        novoFuncionario.setNome(request.nome());
        novoFuncionario.setCpf(request.cpf());
        novoFuncionario.setDataNascimento(request.dataNascimento());
        novoFuncionario.setSenha(request.senha()); // Lembrete: A senha deve ser criptografada antes de salvar.
        novoFuncionario.setAtivo(true);

        var funcionarioSalvo = usuarioRepository.save(novoFuncionario);
        return funcionarioSalvo.getIdUsuario();
    }

    /**
     * Busca todos os usuários que são funcionários (não pacientes).
     * @return Uma lista de DTOs com informações resumidas dos funcionários.
     */
    @Transactional(readOnly = true)
    public List<FuncionarioResponse> buscarTodos() {
        // Vínculo: O metodo findAllFuncionarios é uma query customizada no FuncionarioRepository
        return funcionarioRepository.findAllFuncionarios().stream()
                .map(this::toFuncionarioResponse)
                .collect(Collectors.toList());
    }

    /**
     * Busca os detalhes completos de um funcionário pelo seu ID.
     * @param id O ID do funcionário.
     * @return Um DTO com os detalhes do funcionário.
     * @throws ResourceNotFoundException se o funcionário não for encontrado.
     */
    @Transactional(readOnly = true)
    public FuncionarioDetalhesResponse buscarDetalhesPorId(Integer id) {
        var funcionario = funcionarioRepository.findFuncionarioById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário com ID " + id + " não encontrado."));
        return FuncionarioDetalhesResponse.from(funcionario);
    }

    /**
     * Atualiza os dados cadastrais de um funcionário.
     * @param id O ID do funcionário.
     * @param request DTO com os novos dados.
     * @return Um DTO com os dados atualizados.
     * @throws ResourceNotFoundException se o funcionário não for encontrado.
     */
    @Transactional
    public FuncionarioDetalhesResponse atualizarFuncionario(Integer id, AtualizarFuncionarioRequest request) {
        var funcionario = funcionarioRepository.findFuncionarioById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário com ID " + id + " não encontrado."));

        funcionario.setNome(request.nome());
        funcionario.setDataNascimento(request.dataNascimento());

        var salvo = usuarioRepository.save(funcionario);
        return FuncionarioDetalhesResponse.from(salvo);
    }

    /**
     * Inativa um funcionário, impedindo seu acesso ao sistema.
     * @param id O ID do funcionário a ser inativado.
     * @throws ResourceNotFoundException se o funcionário não for encontrado.
     */
    @Transactional
    public void inativarFuncionario(Integer id) {
        var funcionario = funcionarioRepository.findFuncionarioById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário com ID " + id + " não encontrado."));
        funcionario.setAtivo(false);
        usuarioRepository.save(funcionario);
    }

    /**
     * Reativa um funcionário, restaurando seu acesso ao sistema.
     * @param id O ID do funcionário a ser reativado.
     * @throws ResourceNotFoundException se o funcionário não for encontrado.
     */
    @Transactional
    public void ativarFuncionario(Integer id) {
        var funcionario = funcionarioRepository.findFuncionarioById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário com ID " + id + " não encontrado."));
        funcionario.setAtivo(true);
        usuarioRepository.save(funcionario);
    }

    /**
     * Atualiza o conjunto de especialidades de um fisioterapeuta.
     * @param idFisioterapeuta O ID do fisioterapeuta.
     * @param idEspecialidades A lista completa de IDs das novas especialidades.
     * @return A entidade Fisioterapeuta atualizada.
     * @throws ResourceNotFoundException se o fisioterapeuta não for encontrado.
     * @throws BusinessRuleException se o ID fornecido não for de um Fisioterapeuta.
     */
    @Transactional
    public Fisioterapeuta atualizarEspecialidades(Integer idFisioterapeuta, List<Integer> idEspecialidades) {
        var usuario = funcionarioRepository.findFuncionarioById(idFisioterapeuta)
                .orElseThrow(() -> new ResourceNotFoundException("Fisioterapeuta com ID " + idFisioterapeuta + " não encontrado."));

        // Validação de Tipo: Garante que a operação só pode ser executada em um Fisioterapeuta.
        if (!(usuario instanceof Fisioterapeuta)) {
            throw new BusinessRuleException("Apenas usuários do tipo Fisioterapeuta podem ter especialidades.");
        }
        Fisioterapeuta fisio = (Fisioterapeuta) usuario;

        // Busca todas as entidades de especialidade com base nos IDs fornecidos.
        var especialidades = new HashSet<>(this.especialidadeRepository.findAllById(idEspecialidades));
        fisio.setEspecialidades(especialidades);
        return usuarioRepository.save(fisio);
    }

    /**
     * Metodo auxiliar privado para converter uma entidade Usuario em um DTO FuncionarioResponse.
     * @param usuario A entidade a ser convertida.
     * @return O DTO com os dados formatados.
     */
    private FuncionarioResponse toFuncionarioResponse(Usuario usuario) {
        String tipo = usuario.getClass().getSimpleName().toUpperCase();
        return new FuncionarioResponse(
                usuario.getIdUsuario(),
                usuario.getNome(),
                tipo,
                usuario.isAtivo()
        );
    }

    /**
     * Lista todos os funcionários que estão com o status ATIVO.
     */
    @Transactional(readOnly = true)
    public List<FuncionarioResponse> listarAtivos() {
        return funcionarioRepository.findAllFuncionariosAtivos().stream()
                .map(this::toFuncionarioResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lista todos os funcionários que estão com o status INATIVO.
     */
    @Transactional(readOnly = true)
    public List<FuncionarioResponse> listarInativos() {
        return funcionarioRepository.findAllFuncionariosInativos().stream()
                .map(this::toFuncionarioResponse)
                .collect(Collectors.toList());
    }
}
