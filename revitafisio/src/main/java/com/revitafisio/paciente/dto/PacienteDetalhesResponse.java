package com.revitafisio.paciente.dto;

import com.revitafisio.entities.usuarios.Contato;
import com.revitafisio.entities.usuarios.Usuario;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DTO para representar os dados detalhados de UM paciente específico.
 * Usado como resposta da API quando se busca um paciente por ID.
 *
 * @param id O ID do paciente.
 * @param nome O nome completo.
 * @param cpf O CPF do paciente.
 * @param dataNascimento A data de nascimento.
 * @param ativo O status atual da conta (true para ativo).
 * @param contatos Um conjunto de DTOs de contato, seguro para ser exposto pela API.
 */
public record PacienteDetalhesResponse(
        Integer id,
        String nome,
        String cpf,
        LocalDate dataNascimento,
        boolean ativo,
        Set<ContatoResponse> contatos
) {
    /**
     * Metodo de fábrica (factory method) estático para converter uma entidade Usuario
     * neste DTO de resposta.
     * @param paciente A entidade Usuario a ser convertida.
     * @return uma nova instância de PacienteDetalhesResponse.
     */
    public static PacienteDetalhesResponse fromEntity(Usuario paciente) {
        // A lógica de conversão, que já estava correta, agora é compatível com a
        // definição do record, pois ambos esperam um Set<ContatoResponse>.
        Set<ContatoResponse> contatosResponse = paciente.getContatos().stream()
                .map(contato -> new ContatoResponse(
                        contato.getIdContato(),
                        contato.getTipo().name(), // Converte enum para String
                        contato.getValor(),
                        contato.isPrincipal()
                ))
                .collect(Collectors.toSet());

        return new PacienteDetalhesResponse(
                paciente.getIdUsuario(),
                paciente.getNome(),
                paciente.getCpf(),
                paciente.getDataNascimento(),
                paciente.isAtivo(),
                contatosResponse // Passa o conjunto de DTOs de contato.
        );
    }
}
