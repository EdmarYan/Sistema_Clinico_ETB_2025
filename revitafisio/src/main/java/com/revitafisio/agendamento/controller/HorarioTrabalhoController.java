package com.revitafisio.agendamento.controller;

import com.revitafisio.agendamento.service.HorarioTrabalhoService;
import com.revitafisio.agendamento.dto.HorarioTrabalhoRequest;
import com.revitafisio.agendamento.dto.HorarioTrabalhoResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Controller REST para gerenciar a grade de horários de trabalho dos fisioterapeutas.
 *
 * Anotações:
 * @RestController: Padrão para APIs REST, indicando que os métodos retornam JSON.
 * @RequestMapping("/horarios-trabalho"): Define o caminho base para todos os endpoints.
 * @Validated: Habilita a validação para parâmetros de metodo, como @RequestParam e @PathVariable.
 */
@RestController
@RequestMapping("/horarios-trabalho")
@Validated
public class HorarioTrabalhoController {

    private final HorarioTrabalhoService horarioTrabalhoService;

    public HorarioTrabalhoController(HorarioTrabalhoService horarioTrabalhoService) {
        this.horarioTrabalhoService = horarioTrabalhoService;
    }

    /**
     * Endpoint para adicionar um novo bloco de horário na grade de um fisioterapeuta.
     * @param request DTO com os dados do novo horário. @Valid ativa as validações.
     * @return Resposta 201 (Created) com a localização e o corpo do novo recurso.
     */
    @PostMapping
    public ResponseEntity<HorarioTrabalhoResponse> adicionarHorario(@Valid @RequestBody HorarioTrabalhoRequest request) {
        // Vínculo: Delega a lógica para o HorarioTrabalhoService.
        var response = horarioTrabalhoService.adicionarHorario(request);

        // Boa prática: Retornar a URI do recurso criado no header 'Location'.
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    /**
     * Endpoint para listar todos os horários de trabalho de um fisioterapeuta específico.
     * @param id O ID do fisioterapeuta, validado para não ser nulo.
     * @return Uma lista com os horários de trabalho e status 200 (OK).
     */
    @GetMapping("/fisioterapeuta/{id}")
    public ResponseEntity<List<HorarioTrabalhoResponse>> listarPorFisioterapeuta(
            @PathVariable @NotNull(message = "O ID do fisioterapeuta é obrigatório.") Integer id) {
        var horarios = horarioTrabalhoService.listarHorariosPorFisioterapeuta(id);
        return ResponseEntity.ok(horarios);
    }

    /**
     * Endpoint para remover um bloco de horário de trabalho da grade.
     * @param id O ID do registro de HorarioTrabalho a ser removido.
     * @return Resposta 204 (No Content) indicando sucesso na remoção.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerHorario(
            @PathVariable @NotNull(message = "O ID do horário de trabalho é obrigatório.") Integer id) {
        horarioTrabalhoService.removerHorario(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint para acionar a geração da agenda de horários disponíveis para um mês inteiro.
     * Esta é uma ação crucial que traduz a grade semanal em slots concretos para agendamento.
     *
     * @param idFisioterapeuta ID do fisioterapeuta.
     * @param ano Ano para o qual a agenda será gerada.
     * @param mes Mês para o qual a agenda será gerada (valor entre 1 e 12).
     * @return Resposta 200 (OK) se o processo for iniciado com sucesso.
     */
    @PostMapping("/gerar-disponibilidade")
    public ResponseEntity<Void> gerarDisponibilidade(
            @RequestParam @NotNull(message = "O ID do fisioterapeuta é obrigatório.") Integer idFisioterapeuta,
            @RequestParam @NotNull(message = "O ano é obrigatório.") @Min(value = 2024, message = "O ano deve ser válido.") int ano,
            @RequestParam @NotNull(message = "O mês é obrigatório.") @Min(value = 1, message = "O mês deve ser entre 1 e 12.") @Max(value = 12, message = "O mês deve ser entre 1 e 12.") int mes) {

        // Vínculo: Chama o metodo no serviço que contém a lógica complexa de geração.
        horarioTrabalhoService.gerarDisponibilidadeParaMes(idFisioterapeuta, ano, mes);
        return ResponseEntity.ok().build();
    }
}
