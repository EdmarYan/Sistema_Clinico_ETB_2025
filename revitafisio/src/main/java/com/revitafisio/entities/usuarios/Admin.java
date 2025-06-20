package com.revitafisio.entities.usuarios;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Representa um usuário com privilégios de Administrador no sistema.
 *
 * Como as demais classes de perfil (Paciente, Fisioterapeuta, etc.), esta classe
 * herda todos os seus atributos e comportamentos da classe abstrata {@link Usuario}.
 * A sua existência serve para diferenciar o tipo de usuário dentro do sistema,
 * permitindo a aplicação de regras de negócio e de segurança específicas.
 *
 * Anotações:
 * @Data: Anotação do Lombok que gera automaticamente getters, setters, etc.
 * @EqualsAndHashCode(callSuper = true): Garante que os campos da classe pai (Usuario)
 * sejam considerados ao gerar os métodos equals() e hashCode(), o que é
 * fundamental para o funcionamento correto da persistência de dados e de coleções.
 * @Entity: Marca esta classe como uma entidade gerenciada pelo JPA.
 * @DiscriminatorValue("ADMIN"): Ponto central da estratégia de herança. Define que,
 * na tabela "usuarios", qualquer registro que represente um administrador
 * terá o valor 'ADMIN' na coluna "tipo_usuario".
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("ADMIN")
public class Admin extends Usuario {
    // Esta classe não possui campos adicionais. Ela funciona como um marcador
    // de tipo para o JPA e para a lógica de negócio, permitindo, por exemplo,
    // que a camada de segurança verifique se um usuário é um 'instanceof Admin'
    // para conceder permissões elevadas.
}
