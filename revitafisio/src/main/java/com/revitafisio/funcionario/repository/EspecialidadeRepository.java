package com.revitafisio.funcionario.repository;

import com.revitafisio.entities.usuarios.Especialidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Interface de Repositório para a entidade {@link Especialidade}.
 *
 * Ao estender JpaRepository<Especialidade, Integer>, o Spring Data JPA
 * automaticamente fornece uma implementação em tempo de execução com todos os
 * métodos CRUD (Create, Read, Update, Delete) básicos para a entidade Especialidade.
 *
 * Anotação:
 * @Repository: Marca esta interface como um componente Spring do tipo Repositório,
 * tornando-a elegível para injeção de dependência e habilitando a tradução
 * de exceções do banco de dados para exceções do Spring.
 */
@Repository
public interface EspecialidadeRepository extends JpaRepository<Especialidade, Integer> {
    // Nenhum metodo customizado é necessário aqui, pois o JpaRepository
    // já fornece todos os métodos necessários para a gestão de especialidades
    // (ex: findAll(), findById(), save(), deleteById()).
}
