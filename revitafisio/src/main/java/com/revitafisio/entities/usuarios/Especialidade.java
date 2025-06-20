package com.revitafisio.entities.usuarios;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa uma especialidade oferecida pela clínica (ex: Ortopedia, Pilates).
 * Esta entidade é utilizada para categorizar os fisioterapeutas e os agendamentos.
 *
 * Anotações:
 * @Data: Anotação do Lombok que gera getters, setters, etc.
 * @Builder: Anotação do Lombok que implementa o padrão de projeto Builder, facilitando a criação de instâncias desta classe.
 * @NoArgsConstructor: Gera um construtor sem argumentos, necessário para o funcionamento do JPA.
 * @AllArgsConstructor: Gera um construtor com todos os campos, útil para o Builder e para testes.
 * @Entity(name = "especialidade"): Define que esta classe é uma entidade JPA. O 'name' é um alias usado em queries JPQL.
 * @Table(name = "especialidades"): Mapeia esta entidade para a tabela "especialidades" no banco de dados.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "especialidade")
@Table(name = "especialidades")
public class Especialidade {

    /**
     * Identificador único da especialidade.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_especialidade")
    private Integer idEspecialidade;

    /**
     * Nome da especialidade (ex: "Ortopedia").
     *
     * Anotações de Validação:
     * @NotBlank: Garante que o nome não pode ser nulo nem conter apenas espaços em branco.
     * Esta validação ocorre na camada de aplicação antes de tentar salvar no banco de dados,
     * fornecendo um feedback mais rápido e claro.
     *
     * Anotações de Persistência:
     * @Column(unique = true, nullable = false): Garante a nível de banco de dados que o nome é único e não pode ser nulo.
     */
    @NotBlank(message = "O nome da especialidade não pode estar em branco.")
    @Column(unique = true, nullable = false, length = 100)
    private String nome;

    /**
     * Cor em formato hexadecimal (ex: "#4e73df") associada à especialidade,
     * utilizada para identificação visual na interface (ex: na agenda).
     *
     * Anotações de Validação:
     * @NotBlank: Garante que o campo da cor não pode ser vazio.
     * @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "O formato da cor deve ser um hexadecimal válido (ex: #RRGGBB)."):
     * Valida se a string da cor segue o padrão de um código hexadecimal.
     */
    @NotBlank(message = "A cor da especialidade não pode estar em branco.")
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "O formato da cor deve ser um hexadecimal válido (ex: #RRGGBB).")
    @Column(nullable = false, length = 7)
    private String cor;
}
