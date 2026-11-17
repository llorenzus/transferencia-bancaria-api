package compass.transferencia_bancaria_api.controller;

import compass.transferencia_bancaria_api.controller.dto.TransacaoResponse;
import compass.transferencia_bancaria_api.controller.dto.TransferenciaAceitaResponse;
import compass.transferencia_bancaria_api.controller.dto.TransferenciaRequest;
import compass.transferencia_bancaria_api.service.TransacaoService;
import compass.transferencia_bancaria_api.service.TransferenciaProducer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transacoes")
@Tag(name = "Transações", description = "Operações de transferência e histórico bancário")
public class TransferenciaController {

    private final TransacaoService transacaoService;
    private final TransferenciaProducer transferenciaProducer;

    public TransferenciaController(TransacaoService transacaoService, TransferenciaProducer transferenciaProducer) {
        this.transacaoService = transacaoService;
        this.transferenciaProducer = transferenciaProducer;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Enfileira a transferência de fundos entre duas contas para processamento assíncrono")
    public ResponseEntity<TransferenciaAceitaResponse> transferir(@RequestBody @Valid TransferenciaRequest request) {
        TransferenciaAceitaResponse response = transferenciaProducer.enviarParaFila(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/historico/{contaId}")
    @Operation(summary = "Consulta o histórico de movimentações financeiras de uma conta")
    public ResponseEntity<List<TransacaoResponse>> buscarHistorico(@PathVariable Long contaId) {
        List<TransacaoResponse> historico = transacaoService.listarMovimentacoes(contaId, contaId);
        return ResponseEntity.ok(historico);
    }

    @GetMapping("/protocolo/{protocolo}")
    @Operation(summary = "Consulta os dados de uma transferência processada a partir do seu protocolo")
    public ResponseEntity<TransacaoResponse> buscarPorProtocolo(@PathVariable String protocolo) {
        return transacaoService.buscarPorProtocolo(protocolo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
