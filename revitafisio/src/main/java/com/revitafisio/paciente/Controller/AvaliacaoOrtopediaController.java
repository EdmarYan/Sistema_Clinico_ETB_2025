package com.revitafisio.paciente.Controller;

import com.revitafisio.entities.paciente.AvaliacaoOrtopedia;
import com.revitafisio.paciente.dto.AvaliacaoOrtopediaRequest;
import com.revitafisio.paciente.service.AvaliacaoOrtopediaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * Controller REST para gerenciar as operações da Avaliação de Ortopedia.
 *
 * Anotações:
 * @RestController: Padrão para APIs REST, indicando que os métodos retornam JSON.
 * @RequestMapping("/avaliacoes/ortopedia"): Define o caminho base para este controller.
 * @Validated: Ativa a validação para parâmetros de metodo, como @PathVariable.
 */
@RestController
@RequestMapping("/avaliacoes/ortopedia")
@Validated
public class AvaliacaoOrtopediaController {

    private final AvaliacaoOrtopediaService avaliacaoService;

    public AvaliacaoOrtopediaController(AvaliacaoOrtopediaService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    /**
     * Endpoint para salvar (criar ou atualizar) uma avaliação de ortopedia.
     * A lógica de decidir se é uma criação ou atualização fica a cargo do serviço.
     *
     * @param request DTO com os dados da avaliação. @Valid ativa as validações.
     * @return Resposta HTTP 201 (Created) se for uma nova avaliação, ou 200 (OK) se for uma atualização.
     */
    @PostMapping
    public ResponseEntity<AvaliacaoOrtopedia> salvarAvaliacao(@Valid @RequestBody AvaliacaoOrtopediaRequest request) {
        // Vínculo: Delega a lógica para o AvaliacaoOrtopediaService.
        var avaliacaoSalva = avaliacaoService.salvar(request);

        // Embora o metodo possa atualizar, ao usar POST, o retorno 200 OK é aceitável.
        // Uma implementação mais complexa poderia verificar se o recurso foi criado
        // e retornar 201 Created com a URI, mas 200 OK é uma abordagem simples e válida.
        return ResponseEntity.ok(avaliacaoSalva);
    }

    /**
     * Endpoint para buscar uma avaliação de ortopedia existente para um paciente específico.
     *
     * @param idPaciente O ID do paciente cuja avaliação será buscada.
     * @return Uma resposta HTTP 200 (OK) com a avaliação se encontrada,
     * ou 404 (Not Found) caso contrário.
     */
    @GetMapping("/paciente/{idPaciente}")
    public ResponseEntity<AvaliacaoOrtopedia> buscarPorPaciente(
            @PathVariable @NotNull(message = "O ID do paciente é obrigatório.") Integer idPaciente) {

        // O serviço retorna um Optional, e o controller o mapeia para a resposta HTTP apropriada.
        return avaliacaoService.buscarPorPaciente(idPaciente)
                .map(ResponseEntity::ok) // Se presente, retorna 200 OK com o objeto.
                .orElse(ResponseEntity.notFound().build()); // Se ausente, retorna 404 Not Found.
    }
}
