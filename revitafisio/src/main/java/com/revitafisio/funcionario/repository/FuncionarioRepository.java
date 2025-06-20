package com.revitafisio.funcionario.repository;

import com.revitafisio.entities.usuarios.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Interface de Repositório para gerenciar entidades do tipo Funcionário.
 *
 * Esta interface estende JpaRepository<Usuario, Integer>, mas é especializada
 * em operações que se aplicam apenas a funcionários (ou seja, todos os tipos de
 * Usuario que NÃO SÃO do tipo Paciente), utilizando queries customizadas para
 * filtrar os resultados na tabela única "usuarios".
 */
@Repository
public interface FuncionarioRepository extends JpaRepository<Usuario, Integer> {

    /**
     * Busca todos os usuários que são funcionários (excluindo pacientes).
     *
     * Anotação e JPQL:
     * @Query: Permite a definição de uma consulta customizada usando JPQL (Java Persistence Query Language).
     * - "TYPE(u) != Paciente": Esta é a cláusula chave para lidar com a herança SINGLE_TABLE.
     * Ela instrui o JPA a filtrar os registros com base no valor da coluna discriminatória,
     * retornando apenas as entidades que não são do tipo 'Paciente'.
     *
     * @return Uma lista de entidades Usuario que são funcionários.
     */
    @Query("SELECT u FROM Usuario u WHERE TYPE(u) != Paciente")
    List<Usuario> findAllFuncionarios();

    /**
     * Busca um único usuário por ID, garantindo que ele seja um funcionário (e não um paciente).
     * @param id O ID do usuário a ser buscado.
     * @return um Optional contendo o usuário se for encontrado e for um funcionário,
     * ou um Optional vazio caso contrário. O uso de Optional é uma boa prática para
     * evitar NullPointerExceptions na camada de serviço.
     */
    @Query("SELECT u FROM Usuario u WHERE u.idUsuario = :id AND TYPE(u) != Paciente")
    Optional<Usuario> findFuncionarioById(Integer id);

    /**
     * Busca todos os funcionários que estão com o status ATIVO.
     * @return Uma lista de funcionários ativos.
     */
    @Query("SELECT u FROM Usuario u WHERE TYPE(u) <> Paciente AND u.ativo = true")
    List<Usuario> findAllFuncionariosAtivos();

    /**
     * Busca todos os funcionários que estão com o status INATIVO.
     * @return Uma lista de funcionários inativos.
     */
    @Query("SELECT u FROM Usuario u WHERE TYPE(u) <> Paciente AND u.ativo = false")
    List<Usuario> findAllFuncionariosInativos();
}
