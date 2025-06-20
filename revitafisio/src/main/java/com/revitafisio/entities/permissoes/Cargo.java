package com.revitafisio.entities.permissoes;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * Representa um cargo ou papel funcional dentro do sistema (ex: RECEPCIONISTA, ADMIN).
 * Um cargo é, essencialmente, um agrupamento de várias permissões.
 *
 * Anotações:
 * @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor: Anotações do Lombok.
 * @Entity e @Table: Configurações padrão do JPA para mapear a classe para a tabela "cargos".
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cargos")
public class Cargo {

    /**
     * Identificador único do cargo.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cargo")
    private Integer idCargo;

    /**
     * O nome do cargo (ex: "ADMINISTRADOR", "FISIOTERAPEUTA").
     *
     * Anotação de Validação:
     * @NotBlank: Impede que um cargo seja criado sem um nome.
     */
    @NotBlank(message = "O nome do cargo é obrigatório.")
    @Column(name = "nome_cargo", unique = true, nullable = false, length = 50)
    private String nomeCargo;

    /**
     * O conjunto de permissões associadas a este cargo.
     * Este campo estabelece um relacionamento Muitos-para-Muitos (ManyToMany) entre Cargo e Permissao.
     *
     * Anotações:
     * @ManyToMany(fetch = FetchType.EAGER): Define o relacionamento.
     * - fetch = FetchType.EAGER: Configura o JPA para sempre carregar a lista de permissões
     * junto com o cargo. Isso é útil para a camada de segurança (Spring Security), que
     * precisa saber todas as permissões do usuário no momento da autenticação.
     *
     * @JoinTable(...): Configura a tabela de junção "cargo_permissoes".
     * - name = "cargo_permissoes": O nome da tabela intermediária.
     * - joinColumns: A coluna que faz referência à entidade atual (Cargo).
     * - inverseJoinColumns: A coluna que faz referência à outra entidade (Permissao).
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "cargo_permissoes",
            joinColumns = @JoinColumn(name = "id_cargo"),
            inverseJoinColumns = @JoinColumn(name = "id_permissao")
    )
    private Set<Permissao> permissoes = new HashSet<>();
}
