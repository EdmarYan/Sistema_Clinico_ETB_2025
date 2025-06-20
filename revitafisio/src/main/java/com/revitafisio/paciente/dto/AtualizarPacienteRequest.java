package com.revitafisio.paciente.dto;

import com.revitafisio.funcionario.dto.CriarContatoRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO (Data Transfer Object) para receber os dados da requisição
 * de atualização dos dados cadastrais de um paciente.
 * Note que campos sensíveis ou imutáveis como CPF não são incluídos.
 *
 * @param nome O novo nome completo do paciente. Não pode ser vazio.
 * @param dataNascimento A nova data de nascimento. Deve ser uma data no passado.
 * @param contatos A lista completa de contatos do paciente. Ao atualizar,
 * a lógica de serviço geralmente substitui a lista antiga por esta nova.
 */
public record AtualizarPacienteRequest(
        @NotBlank(message = "O nome não pode estar em branco.")
        String nome,

        @Past(message = "A data de nascimento deve ser no passado.")
        LocalDate dataNascimento,

        @Valid // Garante que cada item na lista de contatos também seja validado.
        List<CriarContatoRequest> contatos
) {
}
