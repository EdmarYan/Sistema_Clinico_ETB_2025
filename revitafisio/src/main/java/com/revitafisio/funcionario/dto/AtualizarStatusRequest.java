package com.revitafisio.funcionario.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO (Data Transfer Object) genérico para receber uma atualização de status.
 * Usado em operações onde o único dado a ser modificado é um status textual.
 * Exemplo: Atualizar o status de um agendamento.
 *
 * @param novoStatus A string representando o novo status (ex: "REALIZADO", "CANCELADO").
 */
public record AtualizarStatusRequest(
        @NotBlank(message = "O novo status não pode estar em branco.")
        String novoStatus
) {}
