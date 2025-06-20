package com.revitafisio.entities.usuarios;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

/**
 * Representa um usuário do tipo Fisioterapeuta.
 *
 * Esta classe herda de {@link Usuario} e adiciona um relacionamento com a entidade
 * {@link Especialidade}, permitindo que um fisioterapeuta possa ter múltiplas especialidades,
 * e uma especialidade possa ser associada a múltiplos fisioterapeutas.
 *
 * Anotações:
 * @Data e @EqualsAndHashCode(callSuper = true): Anotações do Lombok para geração de código padrão,
 * respeitando os campos da classe pai na herança.
 * @Entity: Marca a classe como uma entidade JPA.
 * @DiscriminatorValue("FISIOTERAPEUTA"): Define o identificador deste tipo de usuário na tabela "usuarios".
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("FISIOTERAPEUTA")
public class Fisioterapeuta extends Usuario {

    /**
     * Conjunto de especialidades associadas a este fisioterapeuta.
     * Este campo estabelece um relacionamento Muitos-para-Muitos (ManyToMany)
     * entre Fisioterapeuta e Especialidade.
     *
     * Anotações:
     * @ManyToMany(fetch = FetchType.EAGER): Define o relacionamento.
     * - fetch = FetchType.EAGER: Configura o JPA para sempre carregar a lista de especialidades
     * junto com o fisioterapeuta em uma única consulta ao banco de dados.
     * Isso é útil aqui pois as especialidades são frequentemente necessárias.
     *
     * @JoinTable(...): Configura a tabela de junção que materializa o relacionamento ManyToMany no banco de dados.
     * - name = "fisioterapeuta_especialidades": O nome da tabela intermediária.
     * - joinColumns = @JoinColumn(name = "fisioterapeuta_id"): Especifica a coluna nesta tabela de junção
     * que armazena a chave estrangeira para a entidade Fisioterapeuta.
     * - inverseJoinColumns = @JoinColumn(name = "especialidade_id"): Especifica a coluna que armazena
     * a chave estrangeira para a outra entidade do relacionamento, Especialidade.
     *
     * Tratamento de Erro (Prevenção de NullPointerException):
     * A coleção é inicializada com "new HashSet<>()". Isso é uma boa prática crucial
     * para evitar um NullPointerException se tentarmos adicionar uma especialidade a um
     * fisioterapeuta recém-criado antes que a coleção seja explicitamente inicializada.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "fisioterapeuta_especialidades",
            joinColumns = @JoinColumn(name = "fisioterapeuta_id"),
            inverseJoinColumns = @JoinColumn(name = "especialidade_id")
    )
    private Set<Especialidade> especialidades = new HashSet<>();

}
