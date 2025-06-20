package com.revitafisio.paciente.Controller;

import com.revitafisio.entities.paciente.AvaliacaoRpg;
import com.revitafisio.paciente.dto.AvaliacaoRpgRequest;
import com.revitafisio.paciente.service.AvaliacaoRpgService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para gerenciar as operações da Avaliação de RPG.
 *
 * Anotações:
 * @RestController: Padrão para APIs REST no Spring.
 * @RequestMapping("/avaliacoes/rpg"): Define o caminho base para este controller.
 * @Validated: Habilita a validação para parâmetros de método, como @PathVariable.
 */
@RestController
@RequestMapping("/avaliacoes/rpg")
@Validated
public class AvaliacaoRpgController {

    private final AvaliacaoRpgService avaliacaoService;

    public AvaliacaoRpgController(AvaliacaoRpgService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    /**
     * Endpoint para salvar (criar ou atualizar) uma avaliação de RPG.
     * A lógica de decidir se a operação é uma criação ou uma atualização
     * é delegada para a camada de serviço.
     *
     * @param request DTO com os dados da avaliação. A anotação @Valid ativa as validações
     * definidas no DTO.
     * @return Resposta HTTP 200 (OK) com a avaliação salva no corpo da resposta.
     */
    @PostMapping
    public ResponseEntity<AvaliacaoRpg> salvarAvaliacao(@Valid @RequestBody AvaliacaoRpgRequest request) {
        // Vínculo: Delega a lógica para o AvaliacaoRpgService.
        var avaliacaoSalva = avaliacaoService.salvar(request);
        return ResponseEntity.ok(avaliacaoSalva);
    }

    /**
     * Endpoint para buscar uma avaliação de RPG existente para um paciente específico.
     *
     * @param idPaciente O ID do paciente cuja avaliação será buscada.
     * @return Uma resposta HTTP 200 (OK) com a avaliação se encontrada,
     * ou 404 (Not Found) caso contrário.
     */
    @GetMapping("/paciente/{idPaciente}")
    public ResponseEntity<AvaliacaoRpg> buscarPorPaciente(
            @PathVariable @NotNull(message = "O ID do paciente é obrigatório.") Integer idPaciente) {
        // O serviço retorna um Optional, e o controller mapeia o resultado para a resposta HTTP correta.
        // Este é um padrão funcional e elegante para lidar com resultados que podem não existir.
        return avaliacaoService.buscarPorPaciente(idPaciente)
                .map(ResponseEntity::ok) // Se o Optional contiver um valor, retorna 200 OK com o valor.
                .orElse(ResponseEntity.notFound().build()); // Se o Optional estiver vazio, retorna 404 Not Found.
    }
}
