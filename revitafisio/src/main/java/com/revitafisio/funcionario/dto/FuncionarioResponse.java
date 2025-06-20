package com.revitafisio.funcionario.dto;

/**
 * DTO (Data Transfer Object) para transportar dados resumidos de um funcionário.
 * Ideal para ser usado em listagens, onde exibir todos os detalhes de cada
 * funcionário seria desnecessário e consumiria mais banda.
 *
 * @param id O ID único do funcionário.
 * @param nome O nome do funcionário.
 * @param tipo O tipo de funcionário (ex: "ADMIN", "RECEPCIONISTA").
 * @param ativo O status da conta do funcionário.
 */
public record FuncionarioResponse(
        Integer id,
        String nome,
        String tipo,
        boolean ativo
) {
}
