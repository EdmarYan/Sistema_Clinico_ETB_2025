package com.revitafisio.agendamento.controller;

import com.revitafisio.agendamento.service.HorarioDisponivelService;
import com.revitafisio.agendamento.dto.HorarioDisponivelResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller REST para consultar os horários disponíveis para agendamento.
 *
 * Anotações:
 * @RestController: Padrão para controllers de API REST no Spring.
 * @RequestMapping("/horarios-disponiveis"): Define o caminho base para este controller.
 * @Validated: Habilita a validação para parâmetros de requisição (@RequestParam, @PathVariable)
 * que possuem anotações de validação (como @NotNull).
 */
@RestController
@RequestMapping("/horarios-disponiveis")
@Validated // Habilita a validação para os parâmetros de método
public class HorarioDisponivelController {

    private final HorarioDisponivelService horarioDisponivelService;

    /**
     * Injeção de dependência do HorarioDisponivelService via construtor.
     * @param horarioDisponivelService A camada de serviço que contém a lógica de negócio.
     */
    public HorarioDisponivelController(HorarioDisponivelService horarioDisponivelService) {
        this.horarioDisponivelService = horarioDisponivelService;
    }

    /**
     * Endpoint para buscar os horários disponíveis de um fisioterapeuta em um período.
     *
     * @param idFisioterapeuta O ID do fisioterapeuta. Não pode ser nulo.
     * @param start A data de início do período da busca (formato YYYY-MM-DD). Não pode ser nula.
     * @param end A data de fim do período da busca (formato YYYY-MM-DD). Não pode ser nula.
     * @return Uma lista de HorarioDisponivelResponse e um status HTTP 200 (OK).
     *
     * Anotações de Parâmetros:
     * @RequestParam: Indica que o valor vem de um parâmetro na URL (ex: ?idFisioterapeuta=1).
     * @NotNull: Validação que garante que o parâmetro não seja nulo. Se for, a API retornará
     * um erro 400 (Bad Request) automaticamente.
     * @DateTimeFormat: Especifica o formato esperado para a data, garantindo que o Spring
     * consiga converter a string da URL para um objeto LocalDate.
     */
    @GetMapping
    public ResponseEntity<List<HorarioDisponivelResponse>> buscarDisponibilidade(
            @RequestParam @NotNull(message = "O ID do fisioterapeuta é obrigatório.") Integer idFisioterapeuta,
            @RequestParam @NotNull(message = "A data de início é obrigatória.") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @NotNull(message = "A data de fim é obrigatória.") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        // Delega a lógica de busca para a camada de serviço.
        // Vínculo: O metodo buscarPorFisioEPeriodo está em HorarioDisponivelService.
        var horarios = horarioDisponivelService.buscarPorFisioEPeriodo(idFisioterapeuta, start, end);
        return ResponseEntity.ok(horarios);
    }
}
