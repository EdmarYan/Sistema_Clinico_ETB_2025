package com.revitafisio.paciente.repository;

import com.revitafisio.entities.usuarios.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Interface de Repositório específica para a entidade {@link Paciente}.
 *
 * Embora herde de JpaRepository<Paciente, Integer>, o que já filtra para retornar
 * apenas entidades do tipo Paciente, os métodos customizados adicionam filtros
 * de negócio importantes, como status de ativação e busca por nome.
 */
@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Integer> {

    /**
     * Busca todos os pacientes que estão com o status ATIVO.
     * @return Uma lista de pacientes ativos.
     */
    List<Paciente> findAllByAtivoTrue();

    /**
     * Busca pacientes ativos cujo nome contenha o termo fornecido, ignorando
     * a diferença entre maiúsculas e minúsculas.
     * - `Containing`: Corresponde à cláusula SQL "LIKE '%nome%'".
     * - `IgnoreCase`: Torna a busca insensível a maiúsculas/minúsculas.
     *
     * @param nome O termo a ser buscado no nome dos pacientes.
     * @return Uma lista de pacientes ativos que correspondem à busca.
     */
    List<Paciente> findByAtivoTrueAndNomeContainingIgnoreCase(String nome);

    /**
     * Busca todos os pacientes que estão com o status INATIVO.
     * @return Uma lista de pacientes inativos.
     */
    List<Paciente> findAllByAtivoFalse();

    /**
     * Busca um paciente ativo pelo seu CPF.
     * @return um Optional contendo o paciente se encontrado e ativo.
     */
    Optional<Paciente> findByAtivoTrueAndCpf(String cpf);
}
