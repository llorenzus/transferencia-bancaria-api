package compass.transferencia_bancaria_api.domain.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transacoes")
public class Transacao {
    public Transacao(){

    }
    public Transacao(Conta origem, Conta destino, LocalDateTime dataHora, BigDecimal valor) {
        this.origem = origem;
        this.destino = destino;
        this.dataHora = dataHora;
        this.valor = valor;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "conta_origem_id", nullable = false)
    private Conta origem;

    @ManyToOne
    @JoinColumn(name = "conta_destino_id", nullable = false)
    private Conta destino;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false)
    private BigDecimal valor;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Conta getOrigem() {
        return origem;
    }

    public Conta getDestino() {
        return destino;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setOrigem(Conta origem) {
        this.origem = origem;
    }

    public void setDestino(Conta destino) {
        this.destino = destino;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
}
