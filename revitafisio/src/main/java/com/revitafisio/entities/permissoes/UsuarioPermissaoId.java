package com.revitafisio.entities.permissoes;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * Representa a chave primária composta para a entidade {@link UsuarioPermissao}.
 *
 * Em JPA, quando uma tabela de junção (como "usuario_permissoes") tem uma chave primária
 * formada por múltiplas colunas, é necessário criar uma classe separada para representar essa chave.
 *
 * Anotações:
 * @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor: Anotações do Lombok para geração de código boilerplate.
 * @Embeddable: Marca esta classe para que ela possa ser "embutida" como parte de outra entidade.
 * É a anotação padrão para classes de chave primária composta.
 *
 * Implementação:
 * Serializable: É um requisito do JPA que todas as classes de chave primária (simples ou compostas)
 * implementem a interface Serializable.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class UsuarioPermissaoId implements Serializable {

    /**
     * Corresponde à parte 'usuario_id' da chave primária composta.
     * Este valor será a chave estrangeira para a tabela 'usuarios'.
     *
     * Anotação de Validação:
     * @NotNull: Garante que o ID do usuário não pode ser nulo ao criar a permissão.
     */
    @NotNull
    @Column(name = "usuario_id")
    private Integer usuarioId;

    /**
     * Corresponde à parte 'permissao_id' da chave primária composta.
     * Este valor será a chave estrangeira para a tabela 'permissoes'.
     *
     * Anotação de Validação:
     * @NotNull: Garante que o ID da permissão não pode ser nulo.
     */
    @NotNull
    @Column(name = "permissao_id")
    private Integer permissaoId;
}
