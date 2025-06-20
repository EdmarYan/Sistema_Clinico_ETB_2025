package com.revitafisio.entities.usuarios;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Classe Abstrata de Usuário.
 * Esta é a classe base para todas as entidades que representam um usuário no sistema,
 * como Paciente, Fisioterapeuta, Recepcionista e Admin.
 *
 * Utiliza a estratégia de herança SINGLE_TABLE, o que significa que todos os tipos de usuários
 * serão armazenados em uma única tabela no banco de dados chamada "usuarios".
 *
 * Anotações:
 * @Data: Anotação do Lombok que gera automaticamente getters, setters, toString(), equals() e hashCode().
 * Isso reduz drasticamente o código repetitivo.
 * @Entity: Marca esta classe como uma entidade JPA, ou seja, um objeto que será mapeado para uma tabela no banco de dados.
 * @Table(name = "usuarios"): Especifica que o nome da tabela no banco de dados correspondente a esta entidade é "usuarios".
 * @Inheritance(strategy = InheritanceType.SINGLE_TABLE): Define a estratégia de herança. SINGLE_TABLE é eficiente e
 * cria uma única tabela para a classe pai e todas as suas filhas.
 * @DiscriminatorColumn(...): Cria uma coluna chamada "tipo_usuario" na tabela "usuarios". O valor nesta coluna
 * identificará qual subclasse cada registro representa (ex: 'PACIENTE', 'FISIOTERAPEUTA').
 */
@Data
@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_usuario", discriminatorType = DiscriminatorType.STRING)
public abstract class Usuario {

    /**
     * Identificador único do usuário.
     *
     * Anotações:
     * @Id: Define este campo como a chave primária da tabela.
     * @GeneratedValue(strategy = GenerationType.IDENTITY): Configura a geração automática do valor da chave primária.
     * A estratégia IDENTITY delega a geração para o próprio banco de dados (auto-incremento).
     * @Column(name = "id_usuario"): Mapeia este campo para a coluna "id_usuario" na tabela.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    /**
     * Nome completo do usuário.
     *
     * Anotações:
     * @Column(nullable = false, length = 255): Garante que este campo não pode ser nulo no banco de dados
     * e define o tamanho máximo da coluna como 255 caracteres, alinhado ao DDL.
     */
    @Column(nullable = false, length = 255)
    private String nome;

    /**
     * CPF (Cadastro de Pessoas Físicas) do usuário.
     *
     * Anotações:
     * @Column(unique = true, nullable = false, length = 14): Garante que cada CPF é único no sistema,
     * não pode ser nulo e tem um tamanho máximo de 14 caracteres (para incluir a máscara ###.###.###-##).
     */
    @Column(unique = true, nullable = false, length = 14)
    private String cpf;

    /**
     * Data de nascimento do usuário.
     *
     * Anotação:
     * @Column(name = "data_nascimento"): Mapeia este campo para a coluna "data_nascimento".
     */
    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    /**
     * Senha de acesso do usuário (deve ser armazenada de forma criptografada).
     *
     * Anotação:
     * @Column(nullable = false, length = 255): Garante que a senha não pode ser nula e define o tamanho máximo.
     * O tamanho grande é para acomodar a senha após a criptografia (hash).
     */
    @Column(nullable = false, length = 255)
    private String senha;

    /**
     * Data e hora em que o registro do usuário foi criado.
     *
     * Anotação:
     * @Column(... insertable = false, updatable = false): Configura este campo para ser gerenciado pelo banco de dados.
     * Ele não será incluído nos comandos INSERT ou UPDATE gerados pelo JPA, pois o banco de dados
     * atribuirá o valor padrão (geralmente o timestamp atual) na criação do registro.
     */
    @Column(name = "data_cadastro", insertable = false, updatable = false)
    private LocalDateTime dataCadastro;

    /**
     * Conjunto de contatos associados a este usuário (telefones, emails).
     * Este é o lado "pai" do relacionamento um-para-muitos com a entidade Contato.
     *
     * Anotações:
     * @OneToMany(mappedBy = "usuario", ...): Define um relacionamento um-para-muitos.
     * - mappedBy = "usuario": Indica que o lado "filho" (a classe Contato) é o dono do relacionamento,
     * através do seu campo chamado "usuario".
     * - cascade = CascadeType.ALL: Propaga todas as operações de persistência (salvar, atualizar, deletar)
     * do Usuário para seus Contatos. Se um usuário for deletado, seus contatos também serão.
     * - orphanRemoval = true: Garante que se um Contato for removido da lista `contatos` deste Usuário,
     * ele será automaticamente deletado do banco de dados.
     * @JsonManagedReference: Anotação do Jackson para gerenciar a serialização bidirecional. Ela indica que este é
     * o lado "pai" da relação e que ele deve ser serializado normalmente. A anotação correspondente
     * `@JsonBackReference` deve estar na entidade Contato para evitar um loop infinito durante a conversão para JSON.
     * @EqualsAndHashCode.Exclude: Anotação do Lombok para excluir este campo dos métodos equals() e hashCode()
     * para evitar recursão infinita e problemas de performance.
     */
    @JsonManagedReference
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Contato> contatos;

    /**
     * Campo que controla o status do usuário (ativo ou inativo).
     * Um usuário inativo não pode fazer login ou ser usado em novas operações.
     * O valor padrão é 'true' (ativo).
     */
    private boolean ativo = true;
}
