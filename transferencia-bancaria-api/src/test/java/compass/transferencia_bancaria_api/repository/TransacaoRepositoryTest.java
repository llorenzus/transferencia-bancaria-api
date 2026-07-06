package compass.transferencia_bancaria_api.repository;

import compass.transferencia_bancaria_api.domain.model.Conta;
import compass.transferencia_bancaria_api.domain.model.Transacao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TransacaoRepositoryTest {

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Autowired
    private ContaRepository contaRepository;

    @Test
    void deveBuscarTransacoesPorOrigemOuDestino() {

        Conta origem = new Conta();
        origem.setNome("Origem");
        origem.setSaldo(BigDecimal.valueOf(1000));
        origem = contaRepository.save(origem);

        Conta destino = new Conta();
        destino.setNome("Destino");
        destino.setSaldo(BigDecimal.valueOf(500));
        destino = contaRepository.save(destino);

        // Transação 1
        Transacao t1 = new Transacao();
        t1.setOrigem(origem);
        t1.setDestino(destino);
        t1.setValor(BigDecimal.TEN);
        t1.setDataHora(LocalDateTime.now());
        transacaoRepository.save(t1);

        // Transação 2
        Transacao t2 = new Transacao();
        t2.setOrigem(destino);
        t2.setDestino(origem);
        t2.setValor(BigDecimal.valueOf(20));
        t2.setDataHora(LocalDateTime.now());
        transacaoRepository.save(t2);

        // EXECUÇÃO
        List<Transacao> resultado =
                transacaoRepository.findByOrigemIdOrDestinoId(origem.getId(), origem.getId());

        // ASSERTS
        assertEquals(2, resultado.size());

        assertTrue(resultado.stream()
                .anyMatch(t -> t.getValor().compareTo(BigDecimal.TEN) == 0));

        assertTrue(resultado.stream()
                .anyMatch(t -> t.getValor().compareTo(BigDecimal.valueOf(20)) == 0));
    }
}