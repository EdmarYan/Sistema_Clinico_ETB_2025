package com.revitafisio.agendamento.controller;

import com.revitafisio.agendamento.dto.AgendamentoResponse;
import com.revitafisio.funcionario.dto.AtualizarStatusRequest;
import com.revitafisio.agendamento.dto.CriarAgendamentoRequest;
import com.revitafisio.agendamento.service.AgendamentoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller REST para gerenciar todas as operações relacionadas a agendamentos.
 *
 * Anotações:
 * @RestController: Marca esta classe como um controller Spring onde os métodos retornam
 * dados diretamente no corpo da resposta HTTP (JSON por padrão).
 * @RequestMapping("/agendamentos"): Define o caminho base para todos os endpoints
 * deste controller. Todas as URLs aqui começarão com "/agendamentos".
 */
@RestController
@RequestMapping("/agendamentos")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    /**
     * Injeção de dependência do AgendamentoService via construtor.
     * Esta é a prática recomendada pelo Spring para garantir que as dependências
     * sejam obrigatórias e imutáveis.
     * @param agendamentoService A camada de serviço que contém a lógica de negócio.
     */
    public AgendamentoController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    /**
     * Endpoint para criar um novo agendamento.
     *
     * @param request DTO contendo os dados para o novo agendamento.
     * A anotação @Valid ativa a validação dos campos deste objeto.
     * @return Uma resposta HTTP 201 (Created) com a localização do novo recurso
     * e o agendamento criado no corpo da resposta.
     */
    @PostMapping
    public ResponseEntity<AgendamentoResponse> criarAgendamento(
            @Valid @RequestBody CriarAgendamentoRequest request
    ) {
        // Delega a criação para a camada de serviço.
        // Vínculo: O metodo criarAgendamento está em AgendamentoService.
        var agendamento = agendamentoService.criarAgendamento(request);

        // REFATORAÇÃO: Em vez de retornar 200 OK, estamos retornando 201 Created,
        // que é o status HTTP correto para a criação de um recurso.
        // Construímos a URI do novo recurso criado para incluir no header "Location".
        // CORREÇÃO: O metodo de acesso ao ID no DTO de resposta foi corrigido.
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(agendamento.id())
                .toUri();

        return ResponseEntity.created(location).body(agendamento);
    }

    /**
     * Endpoint para buscar a agenda de um fisioterapeuta em um determinado período.
     *
     * @param idFisioterapeuta ID do fisioterapeuta.
     * @param inicio Data e hora de início do período da busca.
     * @param fim Data e hora de fim do período da busca.
     * @return Uma lista de AgendamentoResponse e um status HTTP 200 (OK).
     */
    @GetMapping
    public ResponseEntity<List<AgendamentoResponse>> buscarAgenda(
            @RequestParam Integer idFisioterapeuta,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim
    ) {
        var agenda = agendamentoService.buscarAgenda(idFisioterapeuta, inicio, fim);
        return ResponseEntity.ok(agenda);
    }

    /**
     * Endpoint para buscar todos os agendamentos que estão com status pendente de ação.
     *
     * @return Uma lista de agendamentos pendentes e um status HTTP 200 (OK).
     */
    @GetMapping("/pendentes-status")
    public ResponseEntity<List<AgendamentoResponse>> buscarPendentes() {
        return ResponseEntity.ok(agendamentoService.buscarAgendamentosPendentesDeStatus());
    }

    /**
     * Endpoint para atualizar o status de um agendamento existente (ex: para REALIZADO ou CANCELADO).
     *
     * @param id O ID do agendamento a ser atualizado, vindo da URL.
     * @param request DTO contendo o novo status.
     * @return Uma resposta HTTP 204 (No Content), indicando que a operação foi bem-sucedida
     * mas não há conteúdo para retornar no corpo.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> atualizarStatus(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarStatusRequest request) {
        // Vínculo: O metodo atualizarStatus está em AgendamentoService.
        agendamentoService.atualizarStatus(id, request.novoStatus());
        return ResponseEntity.noContent().build();
    }
}
