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

    @Test
    public void testAdicionarHorario() throws Exception {
        Fisioterapeuta fisio = new Fisioterapeuta();
        fisio.setNome("Fisio Test");
        fisio.setCpf("999.888.777-66");
        fisio.setSenha("1234");
        fisio = funcionarioRepository.save(fisio);

        String token = jwtService.gerarToken(fisio.getIdUsuario(), "FISIOTERAPEUTA");

        HorarioTrabalhoRequest request = new HorarioTrabalhoRequest(
                fisio.getIdUsuario(),
                DayOfWeek.MONDAY,
                LocalTime.of(8, 0),
                LocalTime.of(12, 0)
        );

        mockMvc.perform(post("/horarios-trabalho")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
