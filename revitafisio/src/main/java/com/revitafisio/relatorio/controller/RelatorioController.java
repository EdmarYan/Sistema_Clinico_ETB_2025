package com.revitafisio.relatorio.controller;

import com.revitafisio.funcionario.dto.RelatorioAtendimentoResponse;
import com.revitafisio.relatorio.service.RelatorioService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller REST responsável pela geração de relatórios gerenciais.
 *
 * Anotações:
 * @RestController: Padrão para APIs REST no Spring.
 * @RequestMapping("/relatorios"): Define o caminho base para todos os endpoints.
 * @Validated: Habilita a validação para parâmetros de método, como @RequestParam.
 */
@RestController
@RequestMapping("/relatorios")
@Validated
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    /**
     * Endpoint para gerar o relatório de atendimentos realizados em um determinado mês/ano,
     * agrupados por fisioterapeuta.
     *
     * @param ano O ano para o qual o relatório será gerado (deve ser um valor válido, ex: 2024 ou maior).
     * @param mes O mês para o qual o relatório será gerado (deve ser um valor entre 1 e 12).
     * @return Uma lista de DTOs {@link RelatorioAtendimentoResponse} e um status HTTP 200 (OK).
     */
    @GetMapping("/atendimentos-mensal")
    public ResponseEntity<List<RelatorioAtendimentoResponse>> getRelatorioAtendimentos(
            @RequestParam @NotNull(message = "O ano é obrigatório.") @Min(value = 2024, message = "Ano inválido.") int ano,
            @RequestParam @NotNull(message = "O mês é obrigatório.") @Min(value = 1, message = "O mês deve estar entre 1 e 12.") @Max(value = 12, message = "O mês deve estar entre 1 e 12.") int mes
    ) {
        // Vínculo: Delega a lógica de geração do relatório para o RelatorioService.
        var relatorio = relatorioService.gerarRelatorioAtendimentos(ano, mes);
        return ResponseEntity.ok(relatorio);
    }
}
