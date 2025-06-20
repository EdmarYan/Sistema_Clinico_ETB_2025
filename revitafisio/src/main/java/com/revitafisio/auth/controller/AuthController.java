package com.revitafisio.auth.controller;

import com.revitafisio.auth.dto.AuthRequest;
import com.revitafisio.auth.dto.AuthResponse;
import com.revitafisio.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller responsável por gerenciar a autenticação de usuários.
 * Expõe os endpoints da API relacionados ao processo de login.
 *
 * Anotações:
 * @RestController: Combina @Controller e @ResponseBody, indicando que os métodos desta classe
 * retornarão dados diretamente no corpo da resposta HTTP (geralmente em JSON),
 * e não nomes de views. É o padrão para criação de APIs REST.
 * @RequestMapping("/auth"): Define o prefixo do caminho para todos os endpoints neste controller.
 * Ou seja, todos os endpoints aqui começarão com "/auth".
 *
 * REFATORAÇÃO: O mapeamento foi alterado de "/login" para "/auth" para seguir as convenções REST,
 * onde o path base representa o recurso (autenticação) e os métodos representam as ações (login).
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    /**
     * Injeção de dependência do AuthService.
     * O controller não contém lógica de negócio; ele delega a responsabilidade
     * de autenticar o usuário para a camada de serviço (AuthService).
     * O uso de 'final' e a injeção via construtor é a prática recomendada pelo Spring
     * para garantir que a dependência seja obrigatória e imutável.
     */
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Endpoint para realizar o login de um usuário.
     *
     * @param request O corpo da requisição, contendo o CPF e a senha, mapeado para o DTO {@link AuthRequest}.
     * @return Um ResponseEntity contendo o {@link AuthResponse} com os dados do usuário autenticado e um status HTTP 200 (OK).
     *
     * Anotações:
     * @PostMapping("/login"): Mapeia este metodo para requisições HTTP do tipo POST no caminho "/auth/login".
     * @Valid: Esta anotação ativa a validação dos campos do objeto AuthRequest. Se o request
     * contiver dados inválidos (ex: CPF em branco), o Spring lançará uma exceção
     * (MethodArgumentNotValidException) antes mesmo de o metodo ser executado, retornando um erro 400 (Bad Request).
     * @RequestBody: Indica ao Spring que o corpo da requisição POST deve ser convertido do formato JSON
     * para o objeto Java AuthRequest.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        // Delega a lógica de autenticação para o AuthService.
        // O AuthService será responsável por interagir com o Spring Security e o repositório.
        var respostaLogin = authService.autenticar(request);

        // Se a autenticação for bem-sucedida, retorna os dados do usuário e o status 200 OK.
        // Se o authService lançar uma exceção (ex: credenciais inválidas),
        // o framework a capturará e retornará o código de erro HTTP apropriado (ex: 401, 403).
        return ResponseEntity.ok(respostaLogin);
    }
}
