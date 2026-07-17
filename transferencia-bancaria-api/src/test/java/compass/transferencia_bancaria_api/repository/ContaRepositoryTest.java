package compass.transferencia_bancaria_api.repository;

import compass.transferencia_bancaria_api.domain.model.Conta;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class ContaRepositoryTest {

    @Autowired
    private ContaRepository contaRepository;

    // ----------------------------
    // Salvar e buscar por ID
    // ----------------------------
    @Test
    void deveSalvarEBuscarContaPorId() {

        Conta conta = new Conta();
        conta.setNome("Conta Teste");
        conta.setSaldo(BigDecimal.valueOf(100));

        Conta salva = contaRepository.save(conta);

        Optional<Conta> encontrada = contaRepository.findById(salva.getId());

        assertTrue(encontrada.isPresent());
        assertEquals("Conta Teste", encontrada.get().getNome());
        assertEquals(0, BigDecimal.valueOf(100).compareTo(encontrada.get().getSaldo()));
    }

    // ----------------------------
    // Deve retornar vazio quando não existe
    // ----------------------------
    @Test
    void deveRetornarVazioQuandoContaNaoExiste() {

        Optional<Conta> conta = contaRepository.findById(999L);

        assertTrue(conta.isEmpty());
    }

    // ----------------------------
    // Atualização de saldo
    // ----------------------------
    @Test
    void deveAtualizarSaldoDaConta() {

        Conta conta = new Conta();
        conta.setNome("Conta Atualização");
        conta.setSaldo(BigDecimal.valueOf(50));

        Conta salva = contaRepository.save(conta);

        salva.setSaldo(BigDecimal.valueOf(200));
        contaRepository.save(salva);

        Conta atualizada = contaRepository.findById(salva.getId()).orElseThrow();

        assertEquals(0, BigDecimal.valueOf(200).compareTo(atualizada.getSaldo()));
    }

    @Test
    void deveBuscarContaComLockPessimista() {

        Conta conta = new Conta();
        conta.setNome("Conta Lock");
        conta.setSaldo(BigDecimal.TEN);

        Conta salva = contaRepository.save(conta);

        Optional<Conta> resultado = contaRepository.findByIdForUpdate(salva.getId());

        assertTrue(resultado.isPresent());
        assertEquals("Conta Lock", resultado.get().getNome());
    }
}