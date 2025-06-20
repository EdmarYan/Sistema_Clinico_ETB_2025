package com.revitafisio.funcionario.dto;

import com.revitafisio.entities.usuarios.Contato;
import com.revitafisio.entities.usuarios.Especialidade;
import com.revitafisio.entities.usuarios.Fisioterapeuta;
import com.revitafisio.entities.usuarios.Usuario;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

/**
 * DTO (Data Transfer Object) para representar os detalhes completos de um funcionário.
 * É utilizado como resposta em endpoints que buscam um funcionário específico por ID.
 *
 * @param idUsuario O ID único do usuário.
 * @param nome O nome completo do funcionário.
 * @param cpf O CPF do funcionário.
 * @param dataNascimento A data de nascimento.
 * @param ativo O status atual da conta (true para ativo, false para inativo).
 * @param tipo_usuario O tipo de funcionário (ex: "FISIOTERAPEUTA", "ADMIN").
 * @param especialidades O conjunto de especialidades (apenas para Fisioterapeutas).
 * @param contatos O conjunto de contatos (e-mail, telefone, etc.) do funcionário.
 */
public record FuncionarioDetalhesResponse(
        Integer idUsuario,
        String nome,
        String cpf,
        LocalDate dataNascimento,
        boolean ativo,
        String tipo_usuario,
        Set<Especialidade> especialidades,
        Set<Contato> contatos
) {
    /**
     * Metodo de fábrica (factory method) estático para converter uma entidade {@link Usuario}
     * neste DTO de resposta.
     *
     * Esta é uma prática de design recomendada, pois centraliza a lógica de conversão
     * e mantém o código de serviço mais limpo.
     *
     * @param usuario A entidade Usuario a ser convertida.
     * @return uma nova instância de FuncionarioDetalhesResponse.
     */
    public static FuncionarioDetalhesResponse from(Usuario usuario) {
        // Tratamento de Erro e Lógica de Negócio:
        // Verifica se o usuário é uma instância de Fisioterapeuta.
        // Se for, obtém o conjunto de especialidades.
        // Se não for, retorna um conjunto vazio em vez de nulo, evitando NullPointerExceptions
        // no frontend e simplificando a renderização.
        Set<Especialidade> especialidades = (usuario instanceof Fisioterapeuta) ?
                ((Fisioterapeuta) usuario).getEspecialidades() : Collections.emptySet();
        Set<Contato> contatos = (usuario.getContatos() != null) ?
                usuario.getContatos() : Collections.emptySet();

        return new FuncionarioDetalhesResponse(
                usuario.getIdUsuario(),
                usuario.getNome(),
                usuario.getCpf(),
                usuario.getDataNascimento(),
                usuario.isAtivo(),
                usuario.getClass().getSimpleName().toUpperCase(),
                especialidades,
                contatos
        );
    }
}
