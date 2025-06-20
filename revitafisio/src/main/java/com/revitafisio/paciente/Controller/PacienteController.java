package com.revitafisio.paciente.Controller;

import com.revitafisio.funcionario.dto.CriarPacienteRequest;
import com.revitafisio.paciente.dto.AtualizarPacienteRequest;
import com.revitafisio.paciente.dto.PacienteDetalhesResponse;
import com.revitafisio.paciente.dto.PacienteResponse;
import com.revitafisio.paciente.service.PacienteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Controller REST para gerenciar todas as operações relacionadas a Pacientes.
 *
 * Anotações:
 * @RestController: Padrão para APIs REST, indicando que os métodos retornam JSON.
 * @RequestMapping("/pacientes"): Define o caminho base para todos os endpoints.
 * @Validated: Ativa a validação para parâmetros de metodo, como @PathVariable e @RequestParam.
 */
@RestController
@RequestMapping("/pacientes")
@Validated
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    /**
     * Endpoint para criar um novo paciente.
     * @param request DTO com os dados do novo paciente. A anotação @Valid ativa as validações no DTO.
     * @return Resposta HTTP 201 (Created) com a localização do novo recurso no header.
     */
    @PostMapping
    public ResponseEntity<Void> criarPaciente(@Valid @RequestBody CriarPacienteRequest request) {
        // Vínculo: Delega a criação para o PacienteService.
        var pacienteId = pacienteService.criarPaciente(request);

        // Boa prática REST: Retorna a URI do recurso recém-criado.
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(pacienteId)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    /**
     * Endpoint unificado para buscar pacientes por NOME ou por CPF.
     * - Se a URL for /pacientes?nome=Joao, busca por nome.
     * - Se a URL for /pacientes?cpf=123, busca por CPF.
     * - Se não houver parâmetros, lista todos os ativos.
     * @param nome (Opcional) O nome ou parte do nome a ser buscado.
     * @return Uma lista de pacientes e status 200 (OK).
     */
    @GetMapping
    public ResponseEntity<List<PacienteResponse>> buscarPacientes(
            @RequestParam(name = "nome", required = false) String nome,
            @RequestParam(name = "cpf", required = false) String cpf // NOVO PARÂMETRO
    ) {
        List<PacienteResponse> listaPacientes;

        if (cpf != null && !cpf.isBlank()) {
            // Se um CPF foi fornecido, busca por CPF.
            listaPacientes = pacienteService.buscarPorCpf(cpf);
        } else if (nome != null && !nome.isBlank()) {
            // Se não, se um nome foi fornecido, busca por nome.
            listaPacientes = pacienteService.buscarPorNome(nome);
        } else {
            // Se nenhum dos dois foi fornecido, busca todos os ativos.
            listaPacientes = pacienteService.buscarTodos();
        }
        return ResponseEntity.ok(listaPacientes);
    }

    /**
     * Endpoint para listar todos os pacientes com status INATIVO.
     * @return Lista de pacientes inativos.
     */
    @GetMapping("/inativos")
    public ResponseEntity<List<PacienteResponse>> listarPacientesInativos() {
        var inativos = pacienteService.buscarTodosInativos();
        return ResponseEntity.ok(inativos);
    }

    /**
     * Endpoint para buscar os detalhes completos de um paciente pelo ID.
     * A expressão regular `:[0-9]+` na URL garante que este endpoint só será ativado se o {id} for um número,
     * prevenindo erros e conflitos com outras rotas.
     * @param id O ID do paciente a ser buscado.
     * @return Um DTO com os detalhes do paciente.
     */
    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<PacienteDetalhesResponse> buscarPorId(@PathVariable Integer id) {
        var paciente = pacienteService.buscarPorId(id);
        return ResponseEntity.ok(paciente);
    }

    /**
     * Endpoint para atualizar os dados cadastrais de um paciente.
     * @param id O ID do paciente a ser atualizado.
     * @param request DTO com os novos dados.
     * @return Um DTO com os dados atualizados do paciente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<PacienteDetalhesResponse> atualizarPaciente(
            @PathVariable @NotNull Integer id,
            @Valid @RequestBody AtualizarPacienteRequest request) {
        var pacienteAtualizado = pacienteService.atualizarPaciente(id, request);
        return ResponseEntity.ok(pacienteAtualizado);
    }

    /**
     * Endpoint para inativar um paciente.
     * @param id O ID do paciente a ser inativado.
     * @return Resposta HTTP 204 (No Content) indicando sucesso.
     */
    @PatchMapping("/{id}/inativar")
    public ResponseEntity<Void> inativarPaciente(@PathVariable @NotNull Integer id) {
        pacienteService.inativarPaciente(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint para reativar um paciente.
     * @param id O ID do paciente a ser reativado.
     * @return Resposta HTTP 204 (No Content) indicando sucesso.
     */
    @PatchMapping("/{id}/ativar")
    public ResponseEntity<Void> ativarPaciente(@PathVariable @NotNull Integer id) {
        pacienteService.ativarPaciente(id);
        return ResponseEntity.noContent().build();
    }
}
