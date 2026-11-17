package compass.transferencia_bancaria_api.controller.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransferenciaMensagem implements Serializable {

    private String protocolo;
    private Long idContaOrigem;
    private Long idContaDestino;
    private BigDecimal valorTransferencia;
    private LocalDateTime dataSolicitacao;

    public TransferenciaMensagem() {
    }

    public TransferenciaMensagem(String protocolo, Long idContaOrigem, Long idContaDestino, BigDecimal valorTransferencia, LocalDateTime dataSolicitacao) {
        this.protocolo = protocolo;
        this.idContaOrigem = idContaOrigem;
        this.idContaDestino = idContaDestino;
        this.valorTransferencia = valorTransferencia;
        this.dataSolicitacao = dataSolicitacao;
    }

    public String getProtocolo() {
        return protocolo;
    }

    public void setProtocolo(String protocolo) {
        this.protocolo = protocolo;
    }

    public Long getIdContaOrigem() {
        return idContaOrigem;
    }

    public void setIdContaOrigem(Long idContaOrigem) {
        this.idContaOrigem = idContaOrigem;
    }

    public Long getIdContaDestino() {
        return idContaDestino;
    }

    public void setIdContaDestino(Long idContaDestino) {
        this.idContaDestino = idContaDestino;
    }

    public BigDecimal getValorTransferencia() {
        return valorTransferencia;
    }

    public void setValorTransferencia(BigDecimal valorTransferencia) {
        this.valorTransferencia = valorTransferencia;
    }

    public LocalDateTime getDataSolicitacao() {
        return dataSolicitacao;
    }

    public void setDataSolicitacao(LocalDateTime dataSolicitacao) {
        this.dataSolicitacao = dataSolicitacao;
    }
}
