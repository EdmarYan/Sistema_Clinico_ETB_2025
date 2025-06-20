package com.revitafisio.funcionario.dto;

import com.revitafisio.entities.usuarios.Contato;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO (Data Transfer Object) para receber os dados da requisição de criação
 * de um ÚNICO contato para um usuário.
 *
 * A arquitetura de um contato por requisição é uma abordagem RESTful comum. Para cadastrar
 * múltiplos contatos (ex: um e-mail e um WhatsApp) para o mesmo usuário, o frontend
 * deve realizar chamadas separadas à API para cada contato a ser adicionado.
 *
 * @param tipo O tipo de contato (EMAIL, CELULAR, etc.). Não pode ser nulo.
 * @param valor O valor do contato (o endereço de e-mail, o número do telefone). Não pode ser vazio.
 * @param principal Indica se este é o contato principal daquele tipo.
 */
public record CriarContatoRequest(
        @NotNull(message = "O tipo do contato é obrigatório.")
        Contato.TipoContato tipo,

        @NotBlank(message = "O valor do contato não pode estar em branco.")
        String valor,

        boolean principal
) {}
