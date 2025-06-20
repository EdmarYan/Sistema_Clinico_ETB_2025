package com.revitafisio.entities.permissoes;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa uma permissão granular dentro do sistema.
 * Uma permissão é uma autorização para realizar uma ação específica,
 * como por exemplo, "CRIAR_PACIENTE" ou "CANCELAR_AGENDAMENTO".
 *
 * Anotações:
 * @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor: Anotações do Lombok para geração de código.
 * @Entity(name = "permissoes"): Marca a classe como uma entidade JPA. O alias "permissoes"
 * pode ser usado em queries JPQL.
 * @Table(name = "permissoes"): Mapeia esta entidade para a tabela "permissoes" no banco de dados.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "permissoes")
@Table(name = "permissoes")
public class Permissao {

    /**
     * Identificador único da permissão.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permissao")
    private Integer idPermissao;

    /**
     * Um código único textual para a permissão (ex: "ROLE_ADMIN", "P_EDITAR_PRONTUARIO").
     * Este campo é ideal para ser usado na lógica de segurança do Spring Security.
     *
     * Anotações de Validação:
     * @NotBlank: Garante que o código da permissão não seja nulo ou vazio.
     */
    @NotBlank(message = "O código da permissão é obrigatório.")
    @Column(unique = true, nullable = false, length = 50)
    private String codigo;

    /**
     * Uma descrição legível sobre o que esta permissão autoriza.
     * Ex: "Permite ao usuário cadastrar novos pacientes no sistema."
     */
    @NotBlank(message = "A descrição da permissão é obrigatória.")
    @Column(nullable = false)
    private String descricao;
}
