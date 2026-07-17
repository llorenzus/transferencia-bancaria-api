package compass.transferencia_bancaria_api.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransacaoResponse {

    private Long id;
    private Long idContaOrigem;
    private String nomeContaOrigem;
    private Long idContaDestino;
    private String nomeContaDestino;
    private LocalDateTime dataHora;
    private BigDecimal valor;

    public TransacaoResponse() {}

    public TransacaoResponse(Long id, Long idContaOrigem, String nomeContaOrigem, Long idContaDestino, String nomeContaDestino, LocalDateTime dataHora, BigDecimal valor) {
        this.id = id;
        this.idContaOrigem = idContaOrigem;
        this.nomeContaOrigem = nomeContaOrigem;
        this.idContaDestino = idContaDestino;
        this.nomeContaDestino = nomeContaDestino;
        this.dataHora = dataHora;
        this.valor = valor;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdContaOrigem() {
        return idContaOrigem;
    }

    public void setIdContaOrigem(Long idContaOrigem) {
        this.idContaOrigem = idContaOrigem;
    }

    public String getNomeContaOrigem() {
        return nomeContaOrigem;
    }

    public void setNomeContaOrigem(String nomeContaOrigem) {
        this.nomeContaOrigem = nomeContaOrigem;
    }

    public Long getIdContaDestino() {
        return idContaDestino;
    }

    public void setIdContaDestino(Long idContaDestino) {
        this.idContaDestino = idContaDestino;
    }

    public String getNomeContaDestino() {
        return nomeContaDestino;
    }

    public void setNomeContaDestino(String nomeContaDestino) {
        this.nomeContaDestino = nomeContaDestino;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
}
