package com.revitafisio.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção customizada para violações de regras de negócio.
 * Deve ser usada quando uma operação é inválida por causa de uma condição específica do sistema.
 * Ex: Tentar cadastrar um CPF que já existe, ou tentar agendar uma consulta para um paciente inativo.
 *
 * A anotação @ResponseStatus(HttpStatus.BAD_REQUEST) instrui o Spring a retornar um
 * código de erro HTTP 400 (Bad Request), indicando que a requisição do cliente era inválida.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BusinessRuleException extends RuntimeException {

    /**
     * Construtor que aceita a mensagem da regra de negócio que foi violada.
     * @param message A mensagem explicando o erro.
     * Ex: "CPF já cadastrado no sistema."
     */
    public BusinessRuleException(String message) {
        super(message);
    }
}
