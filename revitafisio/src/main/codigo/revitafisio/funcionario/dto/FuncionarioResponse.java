package com.revitafisio.funcionario.dto;

/**
 * DTO para transportar dados resumidos de um funcionário em listas.
 */
public record FuncionarioResponse(
        Integer id,
        String nome,
        String tipo,
        boolean ativo
) {
}