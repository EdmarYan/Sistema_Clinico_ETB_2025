package com.revitafisio.paciente.repository;

import com.revitafisio.entities.paciente.Evolucao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Interface de Repositório para a entidade {@link Evolucao}.
 */
@Repository
public interface EvolucaoRepository extends JpaRepository<Evolucao, Integer> {

    /**
     * Busca todas as evoluções de um paciente específico e as ordena pela data em ordem decrescente.
     *
     * O nome deste metodo é um exemplo do poder do Spring Data JPA:
     * - `findByPacienteIdUsuario`: Filtra pelo ID do paciente.
     * - `OrderByDataDesc`: Instrui o JPA a adicionar uma cláusula "ORDER BY data DESC" na query SQL,
     * garantindo que os registros mais recentes venham primeiro na lista.
     *
     * Vínculo: Usado pelo {@link com.revitafisio.paciente.service.EvolucaoService} para
     * exibir o histórico de evoluções na tela de prontuário.
     *
     * @param idPaciente O ID do usuário (paciente).
     * @return Uma lista de evoluções ordenadas da mais nova para a mais antiga.
     */
    List<Evolucao> findByPacienteIdUsuarioOrderByDataDesc(Integer idPaciente);
}
