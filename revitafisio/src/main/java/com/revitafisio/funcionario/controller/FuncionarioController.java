package com.revitafisio.funcionario.controller;

import com.revitafisio.funcionario.service.FuncionarioService;
import com.revitafisio.funcionario.dto.AtualizarFuncionarioRequest;
import com.revitafisio.funcionario.dto.CriarFuncionarioRequest;
import com.revitafisio.funcionario.dto.FuncionarioDetalhesResponse;
import com.revitafisio.funcionario.dto.FuncionarioResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/funcionarios")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    public FuncionarioController(FuncionarioService funcionarioService) {
        this.funcionarioService = funcionarioService;
    }

    @PostMapping
    public ResponseEntity<Void> criarFuncionario(@RequestBody CriarFuncionarioRequest request) {
        var funcionarioId = funcionarioService.criarFuncionario(request);
        return ResponseEntity.created(URI.create("/funcionarios/" + funcionarioId)).build();
    }

    @GetMapping
    public ResponseEntity<List<FuncionarioResponse>> listarFuncionarios() {
        var funcionarios = funcionarioService.buscarTodos();
        return ResponseEntity.ok(funcionarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FuncionarioDetalhesResponse> buscarPorId(@PathVariable Integer id) {
        var funcionarioDto = funcionarioService.buscarDetalhesPorId(id);
        return ResponseEntity.ok(funcionarioDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FuncionarioDetalhesResponse> atualizarFuncionario(@PathVariable Integer id, @RequestBody AtualizarFuncionarioRequest request) {
        var funcionarioAtualizado = funcionarioService.atualizarFuncionario(id, request);
        return ResponseEntity.ok(funcionarioAtualizado);
    }

    @PutMapping("/{id}/especialidades")
    public ResponseEntity<FuncionarioDetalhesResponse> atualizarEspecialidades(@PathVariable Integer id, @RequestBody List<Integer> idEspecialidades) {
        var fisioAtualizado = funcionarioService.atualizarEspecialidades(id, idEspecialidades);
        return ResponseEntity.ok(FuncionarioDetalhesResponse.from(fisioAtualizado));
    }

    @GetMapping("/ativos")
    public ResponseEntity<List<FuncionarioResponse>> listarFuncionariosAtivos() {
        var funcionarios = funcionarioService.listarAtivos();
        return ResponseEntity.ok(funcionarios);
    }

    @GetMapping("/inativos")
    public ResponseEntity<List<FuncionarioResponse>> listarFuncionariosInativos() {
        var funcionarios = funcionarioService.listarInativos();
        return ResponseEntity.ok(funcionarios);
    }

    // ## ADICIONADO: Endpoints para ativar e inativar funcionários ##

    @PatchMapping("/{id}/inativar")
    public ResponseEntity<Void> inativarFuncionario(@PathVariable Integer id) {
        funcionarioService.inativarFuncionario(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/ativar")
    public ResponseEntity<Void> ativarFuncionario(@PathVariable Integer id) {
        funcionarioService.ativarFuncionario(id);
        return ResponseEntity.noContent().build();
    }
}
