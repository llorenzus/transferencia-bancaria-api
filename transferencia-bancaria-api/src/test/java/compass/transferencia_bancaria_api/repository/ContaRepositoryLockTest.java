package compass.transferencia_bancaria_api.repository;

import compass.transferencia_bancaria_api.domain.model.Conta;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ContaRepositoryLockTest {

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void deveBloquearConcorrenciaComLockPessimista() throws Exception {

        Conta conta = new Conta();
        conta.setNome("Conta Concorrente");
        conta.setSaldo(BigDecimal.valueOf(100));

        Conta salva = contaRepository.save(conta);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        Runnable task1 = () -> {
            transactionTemplate.execute(status -> {
                contaRepository.findByIdForUpdate(salva.getId())
                        .ifPresent(c -> {
                            try {
                                Thread.sleep(1000); // segura a transação e o lock por 1 segundo
                                c.setSaldo(c.getSaldo().subtract(BigDecimal.TEN));
                                contaRepository.save(c);
                            } catch (InterruptedException ignored) {}
                        });
                return null;
            });
        };

        Runnable task2 = () -> {
            try {
                Thread.sleep(200); // garante que a task1 inicie e adquira o lock primeiro
            } catch (InterruptedException ignored) {}
            transactionTemplate.execute(status -> {
                contaRepository.findByIdForUpdate(salva.getId())
                        .ifPresent(c -> {
                            c.setSaldo(c.getSaldo().subtract(BigDecimal.TEN));
                            contaRepository.save(c);
                        });
                return null;
            });
        };

        executor.submit(task1);
        executor.submit(task2);

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        Conta finalConta = contaRepository.findById(salva.getId()).orElseThrow();

        // O saldo final deve ser exatamente 80, o que comprova que as duas operações rodaram de forma sequencial (segura)
        // Sem o lock pessimista sob transação ativa, ocorreria uma "lost update" e o saldo final seria 90.
        assertEquals(0, BigDecimal.valueOf(80).compareTo(finalConta.getSaldo()));
    }
}