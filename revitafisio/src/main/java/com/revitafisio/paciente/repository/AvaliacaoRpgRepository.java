package com.revitafisio.paciente.repository;

import com.revitafisio.entities.paciente.AvaliacaoRpg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Interface de Repositório para a entidade {@link AvaliacaoRpg}.
 */
@Repository
public interface AvaliacaoRpgRepository extends JpaRepository<AvaliacaoRpg, Integer> {

    /**
     * Busca uma avaliação de RPG pelo ID do paciente associado.
     *
     * Assim como no repositório de ortopedia, este metodo derivado permite encontrar
     * a única avaliação de RPG de um paciente.
     *
     * Vínculo: Usado pelo {@link com.revitafisio.paciente.service.AvaliacaoRpgService}.
     *
     * @param idPaciente O ID do usuário (paciente).
     * @return Um Optional contendo a avaliação se encontrada, ou um Optional vazio.
     */
    Optional<AvaliacaoRpg> findByPacienteIdUsuario(Integer idPaciente);
}
