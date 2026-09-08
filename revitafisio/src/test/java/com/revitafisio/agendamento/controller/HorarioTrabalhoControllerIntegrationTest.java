package com.revitafisio.agendamento.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revitafisio.agendamento.dto.HorarioTrabalhoRequest;
import com.revitafisio.auth.service.JwtService;
import com.revitafisio.entities.usuarios.Fisioterapeuta;
import com.revitafisio.funcionario.repository.FuncionarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Classe de Teste de Integração para o Controlador de Horários de Trabalho.
 * 
 * Este teste não é um teste unitário puro; ele sobe o contexto completo do Spring Boot
 * e simula requisições HTTP reais usando o MockMvc para testar todas as camadas
 * de uma vez (Controller -> Service -> Repository -> Banco de Dados H2 em memória).
 * 
 * Anotações utilizadas:
 * @SpringBootTest: Diz ao Spring para carregar o contexto completo da aplicação para testes.
 * @AutoConfigureMockMvc: Configura automaticamente o MockMvc, que é a ferramenta que usaremos
 * para simular as requisições HTTP (GET, POST, etc.) sem precisar de um servidor web real.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class HorarioTrabalhoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Testa o fluxo completo de adição de um novo horário de trabalho para um Fisioterapeuta.
     * 
     * O que este teste faz:
     * 1. Cria e salva um Fisioterapeuta no banco de dados.
     * 2. Gera um Token JWT real para este usuário recém-criado.
     * 3. Monta o DTO (objeto) da requisição (JSON).
     * 4. Usa o MockMvc para fazer um POST para /horarios-trabalho simulando
     *    um cliente (ex: React, Insomnia) enviando o Token JWT no cabeçalho.
     * 5. Verifica se a resposta foi 201 Created.
     */
    @Test
    public void testAdicionarHorario() throws Exception {
        // 1. Preparação (Arrange): Criando dados no banco
        Fisioterapeuta fisio = new Fisioterapeuta();
        fisio.setNome("Fisio Test");
        fisio.setCpf("999.888.777-66");
        fisio.setSenha("1234");
        fisio = funcionarioRepository.save(fisio);

        // 2. Preparação: Gerando um Token de autorização válido
        String token = jwtService.gerarToken(fisio.getIdUsuario(), "FISIOTERAPEUTA");

        // 3. Preparação: Corpo da Requisição (Payload)
        HorarioTrabalhoRequest request = new HorarioTrabalhoRequest(
                fisio.getIdUsuario(),
                DayOfWeek.MONDAY,
                LocalTime.of(8, 0),
                LocalTime.of(12, 0)
        );

        // 4 e 5. Ação e Verificação (Act & Assert): Simula o POST
        mockMvc.perform(post("/horarios-trabalho")
                .header("Authorization", "Bearer " + token) // Passa o token no cabeçalho
                .contentType(MediaType.APPLICATION_JSON)    // Diz que estamos mandando JSON
                .content(objectMapper.writeValueAsString(request))) // Converte o Objeto Java para JSON
                .andExpect(status().isCreated()); // Espera que o status HTTP seja 201 (Created)
    }
}
