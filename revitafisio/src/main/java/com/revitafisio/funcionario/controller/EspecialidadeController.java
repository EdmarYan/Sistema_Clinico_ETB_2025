package com.revitafisio.funcionario.controller;

import com.revitafisio.entities.usuarios.Especialidade;
import com.revitafisio.funcionario.repository.EspecialidadeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller REST responsável por gerenciar as operações relacionadas às Especialidades.
 * Atualmente, expõe um endpoint para listar todas as especialidades disponíveis.
 *
 * Anotações:
 * @RestController: Padrão para controllers de API REST no Spring, indicando que os métodos
 * retornarão os dados diretamente no corpo da resposta HTTP, em formato JSON.
 * @RequestMapping("/especialidades"): Define o caminho base para todos os endpoints
 * deste controller.
 */
@RestController
@RequestMapping("/especialidades")
public class EspecialidadeController {

    private final EspecialidadeRepository especialidadeRepository;

    /**
     * Injeção de dependência do EspecialidadeRepository via construtor.
     * O controller interage diretamente com o repositório, pois a lógica de "listar todos"
     * é simples e não necessita de uma camada de serviço intermediária.
     *
     * @param especialidadeRepository O repositório para acesso aos dados da entidade Especialidade.
     */
    public EspecialidadeController(EspecialidadeRepository especialidadeRepository) {
        this.especialidadeRepository = especialidadeRepository;
    }

    /**
     * Endpoint para listar todas as especialidades cadastradas no sistema.
     * Este endpoint é fundamental para o frontend popular listas de seleção, como
     * no formulário de agendamento ou no cadastro de fisioterapeutas.
     *
     * Anotação:
     * @GetMapping: Mapeia este metodo para requisições HTTP do tipo GET no caminho "/especialidades".
     *
     * @return um ResponseEntity contendo a lista de todas as entidades {@link Especialidade}
     * e um status HTTP 200 (OK).
     */
    @GetMapping
    public ResponseEntity<List<Especialidade>> listarEspecialidades() {
        // Vínculo: O metodo findAll() é um metodo padrão herdado do JpaRepository,
        // que busca todos os registros da tabela "especialidades".
        List<Especialidade> especialidades = especialidadeRepository.findAll();

        // Retorna a lista encontrada dentro de um ResponseEntity com status 200 OK.
        return ResponseEntity.ok(especialidades);
    }
}
