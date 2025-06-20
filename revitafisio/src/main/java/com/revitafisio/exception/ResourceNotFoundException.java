package com.revitafisio.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção customizada para ser lançada quando um recurso específico não é encontrado no sistema.
 * Ex: buscar um paciente por um ID que não existe.
 *
 * A anotação @ResponseStatus(HttpStatus.NOT_FOUND) instrui o Spring a automaticamente
 * retornar um código de erro HTTP 404 (Not Found) para o frontend sempre que esta exceção
 * for lançada por um Controller, o que simplifica o tratamento de erros.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Construtor que aceita uma mensagem de erro detalhada.
     * @param message A mensagem explicando o que não foi encontrado.
     * Ex: "Paciente com ID 123 não encontrado."
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
