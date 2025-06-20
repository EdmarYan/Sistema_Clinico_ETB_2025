package com.revitafisio.entities.usuarios.repository;

import com.revitafisio.entities.usuarios.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Interface de Repositório para a entidade Usuario.
 * Esta interface é responsável por todas as operações de acesso a dados (CRUD)
 * relacionadas à tabela "usuarios".
 *
 * Ao estender JpaRepository, o Spring Data JPA automaticamente cria uma implementação
 * em tempo de execução com os métodos padrão:
 * - save(), findById(), findAll(), deleteById(), etc.
 *
 * Isso elimina a necessidade de escrever código boilerplate de acesso ao banco de dados.
 *
 * Anotações:
 * @Repository: Marca esta interface como um componente Spring do tipo Repositório.
 * Isso a torna elegível para injeção de dependência em outras classes (como Services)
 * e habilita a tradução de exceções específicas do banco de dados para exceções do Spring.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    /**
     * Busca um usuário pelo seu CPF.
     *
     * Este é um metodo de consulta customizado. O Spring Data JPA utiliza a convenção de nomenclatura
     * "findBy<NomeDoCampo>" para gerar automaticamente a query correspondente em tempo de execução.
     * Neste caso, ele gerará uma consulta SQL equivalente a "SELECT * FROM usuarios WHERE cpf = ?".
     *
     * Tratamento de Erro (Prevenção de NullPointerException):
     * O metodo retorna um {@link Optional<Usuario>} em vez de apenas 'Usuario'.
     * 'Optional' é um contêiner que pode ou não conter um valor não nulo.
     * - Se um usuário com o CPF fornecido for encontrado, o Optional conterá o objeto Usuario.
     * - Se nenhum usuário for encontrado, o Optional estará vazio (em vez de retornar 'null').
     *
     * Esta é uma prática recomendada do Java moderno, pois força quem chama este metodo
     * (por exemplo, o {@link com.revitafisio.auth.service.AuthService} ou o {@link com.revitafisio.funcionario.service.FuncionarioService})
     * a verificar explicitamente se um resultado foi encontrado, prevenindo NullPointerExceptions.
     *
     * @param cpf O CPF do usuário a ser buscado.
     * @return um Optional contendo o usuário encontrado, ou um Optional vazio se não for encontrado.
     */
    Optional<Usuario> findByCpf(String cpf);
}
