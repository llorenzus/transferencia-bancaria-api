package compass.transferencia_bancaria_api.service;

import compass.transferencia_bancaria_api.controller.dto.TransferenciaMensagem;
import compass.transferencia_bancaria_api.domain.exception.SaldoInsuficienteException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferenciaConsumerTest {

    @Mock
    private TransacaoService transacaoService;

    private TransferenciaConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new TransferenciaConsumer(transacaoService);
    }

    @Test
    void deveProcessarMensagemComSucesso() {
        TransferenciaMensagem mensagem = new TransferenciaMensagem(
                "uuid-123", 1L, 2L, BigDecimal.valueOf(100), LocalDateTime.now()
        );

        consumer.processarMensagem(mensagem);

        verify(transacaoService, times(1)).processarTransferenciaEnfileirada(mensagem);
    }

    @Test
    void deveRejeitarSemReenfileirarQuandoOcorrerErroDeNegocio() {
        TransferenciaMensagem mensagem = new TransferenciaMensagem(
                "uuid-123", 1L, 2L, BigDecimal.valueOf(100), LocalDateTime.now()
        );

        doThrow(new SaldoInsuficienteException(1L))
                .when(transacaoService).processarTransferenciaEnfileirada(mensagem);

        assertThrows(
                AmqpRejectAndDontRequeueException.class,
                () -> consumer.processarMensagem(mensagem)
        );
    }

    @Test
    void deveRelancarExcecaoQuandoOcorrerErroInesperadoParaAcionarRetry() {
        TransferenciaMensagem mensagem = new TransferenciaMensagem(
                "uuid-123", 1L, 2L, BigDecimal.valueOf(100), LocalDateTime.now()
        );

        doThrow(new RuntimeException("Falha transitória de banco de dados"))
                .when(transacaoService).processarTransferenciaEnfileirada(mensagem);

        assertThrows(
                RuntimeException.class,
                () -> consumer.processarMensagem(mensagem)
        );
    }
}
