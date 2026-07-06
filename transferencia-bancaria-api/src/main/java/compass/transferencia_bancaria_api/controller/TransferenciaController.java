package compass.transferencia_bancaria_api.controller;

import compass.transferencia_bancaria_api.controller.dto.TransferenciaRequest;
import compass.transferencia_bancaria_api.domain.model.Transacao;
import compass.transferencia_bancaria_api.service.TransacaoService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/transacoes")
public class TransferenciaController {

    private final TransacaoService transacaoService;

    public TransferenciaController(TransacaoService transacaoService){
        this.transacaoService = transacaoService;
    }

    @PostMapping
    @Operation(summary = "Realiza a transferência de fundos entre duas contas")
    public ResponseEntity<String> transferir(@RequestBody @Valid TransferenciaRequest request) {
        try {
            transacaoService.realizarTransferencia(request);
            return ResponseEntity.ok("Transferência concluída com sucesso.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/historico/{contaId}")
    @Operation(summary = "Consulta o histórico de movimentações financeiras de uma conta")
    public ResponseEntity<List<Transacao>> buscarHistorico(@PathVariable Long contaId) {
        List<Transacao> historico = transacaoService.listarMovimentacoes(contaId, contaId);
        return ResponseEntity.ok(historico);
    }

}
