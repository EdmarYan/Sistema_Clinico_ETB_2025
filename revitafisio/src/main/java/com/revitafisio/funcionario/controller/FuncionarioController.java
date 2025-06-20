package com.revitafisio.funcionario.controller;

import com.revitafisio.funcionario.dto.AtualizarFuncionarioRequest;
import com.revitafisio.funcionario.dto.CriarFuncionarioRequest;
import com.revitafisio.funcionario.dto.FuncionarioDetalhesResponse;
import com.revitafisio.funcionario.dto.FuncionarioResponse;
import com.revitafisio.funcionario.service.FuncionarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Controller REST para gerenciar todas as operações relacionadas a Funcionários.
 *
 * Anotações:
 * @RestController: Padrão para APIs REST, fazendo com que os métodos retornem JSON.
 * @RequestMapping("/funcionarios"): Define o caminho base para todos os endpoints.
 * @Validated: Ativa a validação para parâmetros de metodo, como @PathVariable.
 */
@RestController
@RequestMapping("/funcionarios")
@Validated
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    public FuncionarioController(FuncionarioService funcionarioService) {
        this.funcionarioService = funcionarioService;
    }

    /**
     * Endpoint para criar um novo funcionário (Fisioterapeuta, Recepcionista ou Admin).
     * @param request DTO com os dados do novo funcionário.
     * @return Resposta HTTP 201 (Created) com a localização do novo recurso.
     */
    @PostMapping
    public ResponseEntity<Void> criarFuncionario(@Valid @RequestBody CriarFuncionarioRequest request) {
        var funcionarioId = funcionarioService.criarFuncionario(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(funcionarioId)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    /**
     * Endpoint para listar todos os funcionários, independentemente do status.
     * @return Uma lista com os dados resumidos de todos os funcionários.
     */
    @GetMapping
    public ResponseEntity<List<FuncionarioResponse>> listarFuncionarios() {
        var funcionarios = funcionarioService.buscarTodos();
        return ResponseEntity.ok(funcionarios);
    }

    /**
     * Endpoint para buscar os detalhes completos de um funcionário específico pelo ID.
     * @param id O ID do funcionário a ser buscado.
     * @return Um DTO com os detalhes do funcionário e status 200 (OK).
     */
    @GetMapping("/{id}")
    public ResponseEntity<FuncionarioDetalhesResponse> buscarPorId(
            @PathVariable @NotNull(message = "O ID do funcionário é obrigatório.") Integer id) {
        var funcionarioDto = funcionarioService.buscarDetalhesPorId(id);
        return ResponseEntity.ok(funcionarioDto);
    }

    /**
     * Endpoint para atualizar os dados cadastrais de um funcionário.
     * @param id O ID do funcionário a ser atualizado.
     * @param request DTO com os novos dados.
     * @return Um DTO com os dados atualizados do funcionário e status 200 (OK).
     */
    @PutMapping("/{id}")
    public ResponseEntity<FuncionarioDetalhesResponse> atualizarFuncionario(
            @PathVariable @NotNull(message = "O ID do funcionário é obrigatório.") Integer id,
            @Valid @RequestBody AtualizarFuncionarioRequest request) {
        var funcionarioAtualizado = funcionarioService.atualizarFuncionario(id, request);
        return ResponseEntity.ok(funcionarioAtualizado);
    }

    /**
     * Endpoint para definir ou atualizar as especialidades de um fisioterapeuta.
     * @param id O ID do fisioterapeuta.
     * @param idEspecialidades Uma lista com os IDs das novas especialidades.
     * @return Um DTO com os detalhes atualizados do fisioterapeuta.
     */
    @PutMapping("/{id}/especialidades")
    public ResponseEntity<FuncionarioDetalhesResponse> atualizarEspecialidades(
            @PathVariable @NotNull(message = "O ID do fisioterapeuta é obrigatório.") Integer id,
            @RequestBody @NotEmpty(message = "A lista de especialidades não pode estar vazia.") List<Integer> idEspecialidades) {
        var fisioAtualizado = funcionarioService.atualizarEspecialidades(id, idEspecialidades);
        // O metodo 'from' é um padrão comum para conversão estática de Entidade para DTO.
        return ResponseEntity.ok(FuncionarioDetalhesResponse.from(fisioAtualizado));
    }

    /**
     * Endpoint para listar todos os funcionários com status ATIVO.
     * @return Lista de funcionários ativos.
     */
    @GetMapping("/ativos")
    public ResponseEntity<List<FuncionarioResponse>> listarFuncionariosAtivos() {
        var funcionarios = funcionarioService.listarAtivos();
        return ResponseEntity.ok(funcionarios);
    }

    /**
     * Endpoint para listar todos os funcionários com status INATIVO.
     * @return Lista de funcionários inativos.
     */
    @GetMapping("/inativos")
    public ResponseEntity<List<FuncionarioResponse>> listarFuncionariosInativos() {
        var funcionarios = funcionarioService.listarInativos();
        return ResponseEntity.ok(funcionarios);
    }

    /**
     * Endpoint para inativar um funcionário.
     * @param id O ID do funcionário a ser inativado.
     * @return Resposta HTTP 204 (No Content) indicando sucesso.
     */
    @PatchMapping("/{id}/inativar")
    public ResponseEntity<Void> inativarFuncionario(
            @PathVariable @NotNull(message = "O ID do funcionário é obrigatório.") Integer id) {
        funcionarioService.inativarFuncionario(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint para reativar um funcionário.
     * @param id O ID do funcionário a ser reativado.
     * @return Resposta HTTP 204 (No Content) indicando sucesso.
     */
    @PatchMapping("/{id}/ativar")
    public ResponseEntity<Void> ativarFuncionario(
            @PathVariable @NotNull(message = "O ID do funcionário é obrigatório.") Integer id) {
        funcionarioService.ativarFuncionario(id);
        return ResponseEntity.noContent().build();
    }
}
