package compass.transferencia_bancaria_api.repository;

import compass.transferencia_bancaria_api.domain.model.Conta;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

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

    @SpringBootTest
    class ContaRepositoryLockTest {

        @Autowired
        private ContaRepository contaRepository;

        @Test
        void deveBloquearConcorrenciaComLockPessimista() throws Exception {

            Conta conta = new Conta();
            conta.setNome("Conta Concorrente");
            conta.setSaldo(BigDecimal.valueOf(100));

            Conta salva = contaRepository.save(conta);

            ExecutorService executor = Executors.newFixedThreadPool(2);

            CountDownLatch latch = new CountDownLatch(1);

            Runnable task1 = () -> {
                contaRepository.findByIdForUpdate(salva.getId())
                        .ifPresent(c -> {
                            try {
                                latch.await(); // segura transação
                                c.setSaldo(c.getSaldo().subtract(BigDecimal.TEN));
                                contaRepository.save(c);
                            } catch (InterruptedException ignored) {}
                        });
            };

            Runnable task2 = () -> {
                contaRepository.findByIdForUpdate(salva.getId())
                        .ifPresent(c -> {
                            c.setSaldo(c.getSaldo().subtract(BigDecimal.TEN));
                            contaRepository.save(c);
                            latch.countDown();
                        });
            };

            executor.submit(task1);
            executor.submit(task2);

            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);

            Conta finalConta = contaRepository.findById(salva.getId()).orElseThrow();

            assertTrue(finalConta.getSaldo().compareTo(BigDecimal.valueOf(80)) <= 0);
        }
    }
}