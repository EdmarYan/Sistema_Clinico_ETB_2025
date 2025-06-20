package com.revitafisio.entities.usuarios;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Representa um usuário do tipo Paciente no sistema.
 *
 * Esta classe, assim como as outras especializações de Usuario, herda todos os campos
 * e comportamentos básicos (nome, cpf, etc.) da classe pai {@link Usuario}.
 *
 * Anotações:
 * @Data: Anotação do Lombok que gera o código padrão (getters, setters, etc.).
 * @EqualsAndHashCode(callSuper = true): Garante que os campos da classe pai (Usuario)
 * sejam incluídos nos métodos de comparação e hash, essencial para o correto
 * funcionamento de coleções e da persistência de dados.
 * @Entity: Marca a classe como uma entidade JPA.
 * @DiscriminatorValue("PACIENTE"): Define 'PACIENTE' como o valor identificador
 * na coluna "tipo_usuario" da tabela "usuarios". Isso permite ao JPA
 * instanciar um objeto do tipo correto (Paciente) ao carregar um registro
 * com este valor.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("PACIENTE")
public class Paciente extends Usuario {
    // No momento, esta classe não possui campos específicos que pertençam
    // apenas a um Paciente. Se no futuro for necessário armazenar informações
    // exclusivas de pacientes (ex: número do convênio, histórico médico familiar),
    // os novos campos seriam adicionados aqui.
}
