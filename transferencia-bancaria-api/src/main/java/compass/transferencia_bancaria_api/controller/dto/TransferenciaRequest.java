package compass.transferencia_bancaria_api.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class TransferenciaRequest {

    @NotNull(message = "Conta de origem é obrigatória.")
    private Long idContaOrigem;
    @NotNull(message = "Conta de destino é obrigatória.")
    private Long idContaDestino;
    @NotNull(message = "Valor é obrigatório.")
    @Positive(message = "O valor deve ser maior que zero.")
    private BigDecimal valorTransferencia;

    public Long getIdContaOrigem() {
        return idContaOrigem;
    }

    public Long getIdContaDestino() {
        return idContaDestino;
    }

    public BigDecimal getValorTransferencia() {
        return valorTransferencia;
    }

    public void setIdContaOrigem(Long idContaOrigem) {
        this.idContaOrigem = idContaOrigem;
    }

    public void setIdContaDestino(Long idContaDestino) {
        this.idContaDestino = idContaDestino;
    }

    public void setValorTransferencia(BigDecimal valorTransferencia) {
        this.valorTransferencia = valorTransferencia;
    }
}
