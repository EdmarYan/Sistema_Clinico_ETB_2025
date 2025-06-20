package com.revitafisio.paciente.Controller;

import com.revitafisio.paciente.dto.EvolucaoResponse;
import com.revitafisio.funcionario.dto.CriarEvolucaoRequest;
import com.revitafisio.paciente.service.EvolucaoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Controller REST para gerenciar as operações relacionadas à Evolução do tratamento dos pacientes.
 *
 * Anotações:
 * @RestController: Padrão para APIs REST no Spring, fazendo com que os métodos retornem JSON.
 * @RequestMapping("/evolucoes"): Define a rota base para todos os endpoints deste controller.
 * @Validated: Habilita a validação para parâmetros de método, como @PathVariable e @RequestParam.
 */
@RestController
@RequestMapping("/evolucoes")
@Validated
public class EvolucaoController {

    private final EvolucaoService evolucaoService;

    public EvolucaoController(EvolucaoService evolucaoService) {
        this.evolucaoService = evolucaoService;
    }

    /**
     * Endpoint para salvar uma nova entrada de evolução no prontuário de um paciente.
     *
     * @param request DTO contendo os dados da nova evolução. A anotação @Valid ativa
     * as validações definidas no DTO {@link CriarEvolucaoRequest}.
     * @return Resposta HTTP 201 (Created) com a localização do novo recurso e o objeto
     * da evolução criada no corpo da resposta.
     */
    @PostMapping
    public ResponseEntity<EvolucaoResponse> salvarEvolucao(@Valid @RequestBody CriarEvolucaoRequest request) {
        // Vínculo: Delega a lógica de negócio para o EvolucaoService.
        var evolucaoSalva = evolucaoService.salvarEvolucao(request);

        // Constrói a URI do novo recurso criado para ser retornada no header 'Location'.
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(evolucaoSalva.idEvolucao())
                .toUri();

        return ResponseEntity.created(location).body(evolucaoSalva);
    }

    /**
     * Endpoint para listar todo o histórico de evoluções de um paciente específico.
     *
     * @param idPaciente O ID do paciente cujas evoluções serão buscadas.
     * @return Uma lista de DTOs {@link EvolucaoResponse} e um status HTTP 200 (OK).
     */
    @GetMapping("/paciente/{idPaciente}")
    public ResponseEntity<List<EvolucaoResponse>> listarPorPaciente(
            @PathVariable @NotNull(message = "O ID do paciente é obrigatório.") Integer idPaciente) {

        // Vínculo: Chama o método de busca no EvolucaoService.
        var listaEvolucoes = evolucaoService.listarEvolucoesPorPaciente(idPaciente);
        return ResponseEntity.ok(listaEvolucoes);
    }
}
