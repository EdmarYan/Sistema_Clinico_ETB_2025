package com.revitafisio.entities.usuarios;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Representa um usuário do tipo Recepcionista no sistema.
 *
 * Esta classe é uma implementação concreta da classe abstrata {@link Usuario}.
 * Ela não adiciona novos campos, herdando todos os atributos e comportamentos
 * da classe pai, como nome, cpf, senha, etc.
 *
 * Anotações:
 * @Data: Anotação do Lombok que gera o código padrão (getters, setters, etc.) para os campos herdados.
 * @EqualsAndHashCode(callSuper = true): É crucial para a herança.
 * - callSuper = true: Instrui o Lombok a incluir os campos da classe pai (Usuario)
 * ao gerar os métodos equals() e hashCode(). Sem isso, dois recepcionistas com
 * dados diferentes poderiam ser considerados iguais, o que seria um bug.
 * @Entity: Marca esta classe como uma entidade JPA gerenciável. Embora os dados sejam
 * armazenados na tabela "usuarios", o JPA reconhece "Recepcionista" como um tipo específico.
 * @DiscriminatorValue("RECEPCIONISTA"): Esta é a anotação chave para a estratégia de herança SINGLE_TABLE.
 * - Ela define o valor que será armazenado na coluna "tipo_usuario" (definida na classe Usuario)
 * sempre que um objeto do tipo Recepcionista for salvo no banco de dados.
 * - É assim que o sistema sabe diferenciar um recepcionista de um fisioterapeuta ou paciente na mesma tabela.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("RECEPCIONISTA")
public class Recepcionista extends Usuario {
    // Esta classe não possui campos próprios. Todos os seus dados e comportamentos
    // são herdados diretamente da classe mãe 'Usuario'.
    // A sua principal função é servir como um "marcador de tipo" para o JPA
    // e para a lógica de negócio do sistema.
}
