package com.revitafisio.paciente.dto;

/**
 * DTO (Data Transfer Object) para representar os dados de um contato
 * que serão enviados como resposta pela API.
 *
 * @param id O ID único do contato.
 * @param tipo O tipo de contato (ex: "EMAIL", "CELULAR").
 * @param valor O valor do contato (o e-mail ou número).
 * @param principal Se este é o contato principal daquele tipo.
 */
public record ContatoResponse(
        Integer id,
        String tipo,
        String valor,
        boolean principal
) {
}
