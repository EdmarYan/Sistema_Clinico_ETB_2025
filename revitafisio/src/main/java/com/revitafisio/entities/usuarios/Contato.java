package com.revitafisio.entities.usuarios;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Representa um meio de contato (telefone, email, etc.) associado a um Usuário.
 * Cada usuário pode ter múltiplos contatos.
 *
 * Anotações:
 * @Data: Anotação do Lombok para gerar getters, setters, etc.
 * @Builder: Padrão de projeto para facilitar a construção de objetos Contato.
 * @NoArgsConstructor e @AllArgsConstructor: Construtores gerados pelo Lombok, essenciais para JPA e Builder.
 * @Entity(name = "contatos"): Marca a classe como uma entidade JPA.
 * @Table(name = "contatos"): Mapeia esta entidade para a tabela "contatos" no banco de dados.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "contatos")
@Table(name = "contatos")
public class Contato {

    /**
     * Identificador único do registro de contato.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contato")
    private Integer idContato;

    /**
     * O usuário ao qual este contato pertence.
     * Este é o lado "filho" e "dono" do relacionamento com a entidade Usuario.
     *
     * Anotações:
     * @ManyToOne(fetch = FetchType.LAZY): Define um relacionamento muitos-para-um (muitos Contatos para um Usuario).
     * - fetch = FetchType.LAZY: Configura o JPA para carregar o objeto Usuario associado apenas
     * quando ele for explicitamente acessado. Isso melhora a performance, evitando carregar
     * dados desnecessários.
     * @JoinColumn(name = "id_usuario", nullable = false): Especifica a coluna de chave estrangeira ("id_usuario")
     * na tabela "contatos" que se conecta à chave primária da tabela "usuarios".
     * - nullable = false: Garante que um contato não pode existir sem estar associado a um usuário.
     * @JsonBackReference: Anotação do Jackson crucial para relacionamentos bidirecionais.
     * - Ela "quebra" o loop de serialização, impedindo que, ao converter um Contato para JSON,
     * o campo 'usuario' seja serializado (o que, por sua vez, tentaria serializar a lista de contatos,
     * causando um loop infinito).
     * @EqualsAndHashCode.Exclude: Exclui este campo dos métodos equals() e hashCode() para prevenir recursão.
     */
    @JsonBackReference
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    /**
     * O tipo do contato, definido pelo enum TipoContato.
     *
     * Anotação:
     * @Enumerated(EnumType.STRING): Instrui o JPA a persistir o nome do enum ("EMAIL", "CELULAR")
     * como uma String no banco de dados, em vez de seu valor numérico (ordinal). Isso torna
     * os dados no banco muito mais legíveis e fáceis de manter.
     * @NotNull: Garante que o tipo do contato não pode ser nulo.
     */
    @NotNull(message = "O tipo de contato é obrigatório.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoContato tipo;

    /**
     * O valor do contato (o número de telefone, o endereço de e-mail, etc.).
     *
     * Anotação de Validação:
     * @NotBlank: Garante que o valor do contato não pode ser vazio.
     */
    @NotBlank(message = "O valor do contato não pode estar em branco.")
    @Column(nullable = false, length = 255)
    private String valor;

    /**
     * Indica se este é o contato principal do usuário para um determinado tipo.
     * Ex: Um usuário pode ter vários telefones, mas apenas um é o principal.
     */
    private boolean principal;

    /**
     * Enum que define os tipos de contato permitidos no sistema.
     * Usar um Enum aqui garante a consistência dos dados, prevenindo
     * a inserção de tipos inválidos.
     */
    public enum TipoContato {
        TELEFONE,
        CELULAR,
        EMAIL,
        WHATSAPP
    }

    /**
     * Validação condicional para o campo 'valor'.
     * Este metodo é um exemplo de como poderíamos adicionar uma validação mais complexa.
     * A anotação @AssertTrue faria o JPA chamar este metodo antes de salvar.
     * Se o tipo for EMAIL, ele verifica se o valor é um email válido.
     *
     * Nota: Para usar @AssertTrue, a dependência de validação (spring-boot-starter-validation) é necessária.
     * @return true se o valor for válido para o tipo, false caso contrário.
     */

    @jakarta.validation.constraints.AssertTrue
    private boolean isValorValido() {
        if (valor == null) return false;

        String digitos = valor.replaceAll("\\D", "");

        switch (tipo) {
            case EMAIL:
                return valor.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
            case CELULAR:
            case WHATSAPP:
                return digitos.length() == 11;
            case TELEFONE:
                return digitos.length() == 10;
            default:
                return true;
        }
    }
}
