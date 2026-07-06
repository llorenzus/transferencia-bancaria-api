package compass.transferencia_bancaria_api.controller;

import compass.transferencia_bancaria_api.controller.dto.TransferenciaRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/contas")
public class ContaController {

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
}
