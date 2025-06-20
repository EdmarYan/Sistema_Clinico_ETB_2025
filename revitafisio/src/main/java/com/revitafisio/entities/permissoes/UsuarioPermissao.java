package com.revitafisio.entities.permissoes;

import com.revitafisio.entities.usuarios.Especialidade;
import com.revitafisio.entities.usuarios.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade que mapeia a tabela de junção "usuario_permissoes".
 *
 * Esta classe é necessária para modelar um relacionamento Muitos-para-Muitos
 * que possui colunas adicionais. No caso do Revitafisio, a relação entre
 * Usuario e Permissao precisa armazenar um dado extra: a qual 'especialidade_id'
 * aquela permissão se aplica, caso seja uma permissão específica.
 *
 * Anotações:
 * @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor: Anotações do Lombok.
 * @Entity e @Table: Configurações padrão do JPA.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuario_permissoes")
public class UsuarioPermissao {

    /**
     * A chave primária composta desta entidade, representada pela classe {@link UsuarioPermissaoId}.
     *
     * Anotação:
     * @EmbeddedId: Indica ao JPA que a chave primária desta entidade é uma classe embutida.
     */
    @EmbeddedId
    private UsuarioPermissaoId id;

    /**
     * O objeto Usuario associado a esta permissão.
     *
     * Anotações:
     * @ManyToOne: Define o relacionamento Muitos-para-Um com a entidade Usuario.
     * @MapsId("usuarioId"): Anotação crucial para chaves compostas. Ela diz ao JPA que o campo 'usuario'
     * desta entidade é o "dono" do valor de 'usuarioId' que está dentro da chave embutida 'id'.
     * Em outras palavras, a chave estrangeira é também parte da chave primária.
     * @JoinColumn(name = "usuario_id"): Especifica o nome da coluna de chave estrangeira no banco.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("usuarioId")
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    /**
     * O objeto Permissao associado a este usuário.
     *
     * Anotações:
     * @ManyToOne: Define o relacionamento Muitos-para-Um com a entidade Permissao.
     * @MapsId("permissaoId"): Assim como acima, mapeia este campo para a parte 'permissaoId' da chave embutida.
     * @JoinColumn(name = "permissao_id"): Especifica a coluna de chave estrangeira.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("permissaoId")
    @JoinColumn(name = "permissao_id")
    private Permissao permissao;

    /**
     * O campo extra que justifica a existência desta entidade.
     * Representa a Especialidade à qual esta permissão específica se aplica.
     * Pode ser nulo, para permissões que são gerais e não ligadas a uma especialidade.
     *
     * Anotação:
     * @ManyToOne: Define o relacionamento com a entidade Especialidade.
     * @JoinColumn(name = "especialidade_id"): Especifica a coluna de chave estrangeira.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "especialidade_id")
    private Especialidade especialidade;
}
