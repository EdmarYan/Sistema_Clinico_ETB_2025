package com.revitafisio.paciente.service;

import com.revitafisio.entities.usuarios.Contato;
import com.revitafisio.entities.usuarios.Paciente;
import com.revitafisio.paciente.repository.PacienteRepository;
import com.revitafisio.paciente.dto.AtualizarPacienteRequest;    // Import do novo DTO
import com.revitafisio.funcionario.dto.CriarContatoRequest;
import com.revitafisio.funcionario.dto.CriarPacienteRequest;
import com.revitafisio.paciente.dto.PacienteDetalhesResponse;  // Import do novo DTO
import com.revitafisio.paciente.dto.PacienteResponse;
import com.revitafisio.entities.usuarios.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PacienteService {

    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;

    public PacienteService(UsuarioRepository usuarioRepository, PacienteRepository pacienteRepository) {
        this.usuarioRepository = usuarioRepository;
        this.pacienteRepository = pacienteRepository;
    }

    @Transactional
    public Integer criarPaciente(CriarPacienteRequest request) {
        if (usuarioRepository.findByCpf(request.cpf()).isPresent()) {
            throw new RuntimeException("CPF já cadastrado.");
        }
        var novoPaciente = new Paciente();
        novoPaciente.setNome(request.nome());
        novoPaciente.setCpf(request.cpf());
        novoPaciente.setDataNascimento(request.dataNascimento());
        novoPaciente.setAtivo(true);

        // MUDANÇA AQUI: Geramos uma senha aleatória e segura que nunca será usada.
        novoPaciente.setSenha(UUID.randomUUID().toString());

        if (request.contatos() != null && !request.contatos().isEmpty()) {
            var contatos = request.contatos().stream()
                    .map(dto -> toContatoEntity(dto, novoPaciente))
                    .collect(Collectors.toSet());
            novoPaciente.setContatos(contatos);
        }
        var pacienteSalvo = usuarioRepository.save(novoPaciente);
        return pacienteSalvo.getIdUsuario();
    }

    public List<PacienteResponse> buscarPorNome(String nome) {
        var pacientes = pacienteRepository.findByAtivoTrueAndNomeContainingIgnoreCase(nome);
        return pacientes.stream()
                .map(this::toPacienteResponse)
                .toList();
    }

    public PacienteDetalhesResponse buscarPorId(Integer id) {
        var paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado."));
        return toPacienteDetalhesResponse(paciente);
    }

    public List<PacienteResponse> buscarTodosInativos() {
        return pacienteRepository.findAllByAtivoFalse()
                .stream()
                .map(this::toPacienteResponse)
                .toList();
    }

    @Transactional
    public PacienteDetalhesResponse atualizarPaciente(Integer id, AtualizarPacienteRequest request) {
        var paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado."));

        if (!paciente.isAtivo()) {
            throw new IllegalStateException("Não é possível atualizar um paciente inativo.");
        }

        paciente.setNome(request.nome());
        paciente.setDataNascimento(request.dataNascimento());
        var pacienteSalvo = usuarioRepository.save(paciente);
        return toPacienteDetalhesResponse(pacienteSalvo);
    }

    @Transactional
    public void inativarPaciente(Integer id) {
        var paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado."));
        paciente.setAtivo(false);
        usuarioRepository.save(paciente);
    }
    @Transactional
    public void ativarPaciente(Integer id) {
        var paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado."));
        // A única diferença é que definimos 'ativo' como true
        paciente.setAtivo(true);
        usuarioRepository.save(paciente);
    }

    // Métodos auxiliares
    private Contato toContatoEntity(CriarContatoRequest dto, Paciente paciente) {
        var contato = new Contato();
        contato.setUsuario(paciente);
        contato.setTipo(dto.tipo());
        contato.setValor(dto.valor());
        contato.setPrincipal(dto.principal());
        return contato;
    }

    private PacienteResponse toPacienteResponse(Paciente paciente) {
        return new PacienteResponse(
                paciente.getIdUsuario(),
                paciente.getNome(),
                paciente.getCpf()
        );
    }

    private PacienteDetalhesResponse toPacienteDetalhesResponse(Paciente paciente) {
        return new PacienteDetalhesResponse(
                paciente.getIdUsuario(),
                paciente.getNome(),
                paciente.getCpf(),
                paciente.getDataNascimento(),
                paciente.isAtivo(),
                paciente.getContatos()
        );
    }
    // NOVO METODO - READ (Buscar Todos)
    public List<PacienteResponse> buscarTodos() {
        return pacienteRepository.findAllByAtivoTrue()
                .stream()
                .map(paciente -> new PacienteResponse(
                        paciente.getIdUsuario(),
                        paciente.getNome(),
                        paciente.getCpf()
                ))
                .toList();
    }
}
