package compass.transferencia_bancaria_api.controller.dto;

import java.time.LocalDateTime;

public class TransferenciaAceitaResponse {

    private String protocolo;
    private String status;
    private String mensagem;
    private LocalDateTime dataSolicitacao;

    public TransferenciaAceitaResponse() {
    }

    public TransferenciaAceitaResponse(String protocolo, String status, String mensagem, LocalDateTime dataSolicitacao) {
        this.protocolo = protocolo;
        this.status = status;
        this.mensagem = mensagem;
        this.dataSolicitacao = dataSolicitacao;
    }

    public String getProtocolo() {
        return protocolo;
    }

    public void setProtocolo(String protocolo) {
        this.protocolo = protocolo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public LocalDateTime getDataSolicitacao() {
        return dataSolicitacao;
    }

    public void setDataSolicitacao(LocalDateTime dataSolicitacao) {
        this.dataSolicitacao = dataSolicitacao;
    }
}
