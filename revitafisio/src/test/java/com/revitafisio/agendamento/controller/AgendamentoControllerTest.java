package com.revitafisio.agendamento.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revitafisio.agendamento.dto.AgendamentoResponse;
import com.revitafisio.agendamento.service.AgendamentoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de Integração para a Camada Web (Controller) usando @WebMvcTest.
 *
 * O que o @WebMvcTest faz?
 * Em vez de subir o aplicativo inteiro (o que demoraria muito e precisaria de banco de dados real),
 * ele sobe APENAS a parte Web (os Controllers) e as configurações do Spring MVC.
 *
 * Ele nos fornece o MockMvc, uma ferramenta poderosa para simular requisições HTTP (GET, POST, etc.)
 * e testar se os Códigos de Status (200, 404), Headers e Corpos JSON estão corretos, sem abrir um
 * navegador ou servidor de verdade.
 */
@WebMvcTest(AgendamentoController.class) // Avisamos o Spring para carregar apenas este Controller
@AutoConfigureMockMvc(addFilters = false) // Desliga os filtros de segurança (JWT) para focarmos APENAS na lógica do Controller
class AgendamentoControllerTest {

    // O "carteiro" que vai enviar as requisições HTTP falsas para o nosso Controller
    @Autowired
    private MockMvc mockMvc;

    // Ferramenta que converte objetos Java para JSON e vice-versa
    @Autowired
    private ObjectMapper objectMapper;

    // O AgendamentoController precisa de um AgendamentoService.
    // Como estamos testando APENAS o Controller, nós falsificamos o Service com o @MockBean.
    // O Spring injeta essa cópia falsa no Controller pra gente.
    @MockBean
    private AgendamentoService agendamentoService;

    @Test
    @DisplayName("GET /agendamentos/pendentes-status deve retornar 200 OK e uma lista JSON")
    void buscarPendentes_DeveRetornarListaEStatus200() throws Exception {
        // 1. Arrange: Preparamos a resposta que o Service "falso" vai devolver.
        AgendamentoResponse mockResponse = new AgendamentoResponse(
                1, 
                "Maria Paciente", 
                "Dr. Pedro Fisioterapeuta", 
                "Fisioterapia Motora", 
                LocalDateTime.now(), 
                LocalDateTime.now().plusHours(1), 
                "PENDENTE"
        );
        when(agendamentoService.buscarAgendamentosPendentesDeStatus())
                .thenReturn(List.of(mockResponse));

        // 2 & 3. Act & Assert: Enviamos a requisição GET e checamos o resultado numa "corrente" de métodos
        mockMvc.perform(get("/agendamentos/pendentes-status")) // Ação
                .andExpect(status().isOk()) // Confere se a resposta HTTP é 200 (OK)
                .andExpect(content().contentType("application/json")) // Confere se o retorno é JSON
                .andExpect(jsonPath("$.size()").value(1)) // Usa JSONPath para checar se a lista tem tamanho 1
                .andExpect(jsonPath("$[0].id").value(1)) // Confere o ID do primeiro item do JSON
                .andExpect(jsonPath("$[0].status").value("PENDENTE")); // Confere o Status no JSON
    }
}
