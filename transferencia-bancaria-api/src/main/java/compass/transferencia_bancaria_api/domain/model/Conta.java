package compass.transferencia_bancaria_api.domain.model;

import compass.transferencia_bancaria_api.domain.exception.SaldoInsuficienteException;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name= "contas")
public class Conta {
    public Conta() {

    }
    public Conta(Long id, String nome, BigDecimal saldo) {
        this.id = id;
        this.nome = nome;
        this.saldo = saldo;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nome;
    @Column(nullable = false)
    private BigDecimal saldo;

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public void debitar(BigDecimal valor) {
        if (this.saldo.compareTo(valor) < 0) {
            throw new SaldoInsuficienteException(this.id);
        }
        this.saldo = this.saldo.subtract(valor);
    }

    public void creditar(BigDecimal valor) {
        this.saldo = this.saldo.add(valor);
    }
}
