package com.revitafisio.paciente.repository;

import com.revitafisio.entities.paciente.AvaliacaoOrtopedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Interface de Repositório para a entidade {@link AvaliacaoOrtopedia}.
 * Herda de JpaRepository para obter automaticamente métodos CRUD básicos.
 */
@Repository
public interface AvaliacaoOrtopediaRepository extends JpaRepository<AvaliacaoOrtopedia, Integer> {

    /**
     * Busca uma avaliação de ortopedia pelo ID do paciente associado.
     *
     * Este é um metodo de consulta derivado (derived query method). O Spring Data JPA
     * interpreta o nome do metodo e gera a query SQL correspondente:
     * "SELECT * FROM avaliacao_ortopedia WHERE id_paciente = ?".
     *
     * O uso de 'Optional' é uma boa prática para evitar NullPointerExceptions,
     * indicando claramente que um resultado pode ou não ser encontrado.
     *
     * Vínculo: Usado pelo {@link com.revitafisio.paciente.service.AvaliacaoOrtopediaService}
     * para verificar se uma avaliação já existe para um paciente.
     *
     * @param idPaciente O ID do usuário (paciente).
     * @return Um Optional contendo a avaliação se encontrada, ou um Optional vazio.
     */
    Optional<AvaliacaoOrtopedia> findByPacienteIdUsuario(Integer idPaciente);
}
