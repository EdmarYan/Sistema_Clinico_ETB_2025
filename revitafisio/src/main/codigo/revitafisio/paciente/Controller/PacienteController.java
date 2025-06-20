package com.revitafisio.paciente.Controller;

import com.revitafisio.paciente.dto.AtualizarPacienteRequest;
import com.revitafisio.funcionario.dto.CriarPacienteRequest;
import com.revitafisio.paciente.dto.PacienteDetalhesResponse;
import com.revitafisio.paciente.dto.PacienteResponse;
import com.revitafisio.paciente.service.PacienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @PostMapping
    public ResponseEntity<Void> criarPaciente(@RequestBody CriarPacienteRequest request) {
        var pacienteId = pacienteService.criarPaciente(request);
        return ResponseEntity.created(URI.create("/pacientes/" + pacienteId)).build();
    }

    /**
     * CORREÇÃO PRINCIPAL: Unifica a busca de pacientes.
     * 1. Se a URL for /pacientes (sem parâmetros), lista todos os ativos.
     * 2. Se a URL for /pacientes?nome=Joao, busca por nome.
     * O @RequestParam 'required = false' torna o parâmetro 'nome' opcional.
     */
    @GetMapping
    public ResponseEntity<List<PacienteResponse>> buscarPacientes(
            @RequestParam(name = "nome", required = false) String nome) {

        if (nome != null && !nome.isBlank()) {
            // Se um nome foi fornecido, busca por nome
            var listaPacientes = pacienteService.buscarPorNome(nome);
            return ResponseEntity.ok(listaPacientes);
        } else {
            // Se nenhum nome foi fornecido, busca todos os pacientes ativos
            var listaPacantes = pacienteService.buscarTodos(); // Este método já busca ativos
            return ResponseEntity.ok(listaPacantes);
        }
    }

    @GetMapping("/inativos")
    public ResponseEntity<List<PacienteResponse>> listarPacientesInativos() {
        var inativos = pacienteService.buscarTodosInativos();
        return ResponseEntity.ok(inativos);
    }

    // Adicionamos uma expressão regular para que esta rota SÓ aceite números como ID.
    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<PacienteDetalhesResponse> buscarPorId(@PathVariable Integer id) {
        var paciente = pacienteService.buscarPorId(id);
        return ResponseEntity.ok(paciente);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PacienteDetalhesResponse> atualizarPaciente(@PathVariable Integer id, @RequestBody AtualizarPacienteRequest request) {
        var pacienteAtualizado = pacienteService.atualizarPaciente(id, request);
        return ResponseEntity.ok(pacienteAtualizado);
    }

    @PatchMapping("/{id}/inativar")
    public ResponseEntity<Void> inativarPaciente(@PathVariable Integer id) {
        pacienteService.inativarPaciente(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/ativar")
    public ResponseEntity<Void> ativarPaciente(@PathVariable Integer id) {
        pacienteService.ativarPaciente(id);
        return ResponseEntity.noContent().build();
    }
}
