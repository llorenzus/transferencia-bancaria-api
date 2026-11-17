package compass.transferencia_bancaria_api.service;

import compass.transferencia_bancaria_api.controller.dto.TransferenciaAceitaResponse;
import compass.transferencia_bancaria_api.controller.dto.TransferenciaMensagem;
import compass.transferencia_bancaria_api.controller.dto.TransferenciaRequest;
import compass.transferencia_bancaria_api.domain.exception.TransferenciaInvalidaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransferenciaProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private TransferenciaProducer producer;

    @BeforeEach
    void setUp() {
        producer = new TransferenciaProducer(rabbitTemplate);
        ReflectionTestUtils.setField(producer, "exchangeName", "transferencia.exchange");
        ReflectionTestUtils.setField(producer, "routingKey", "transferencia.routing-key");
    }

    @Test
    void deveEnviarMensagemParaFilaComSucesso() {
        TransferenciaRequest request = new TransferenciaRequest();
        request.setIdContaOrigem(1L);
        request.setIdContaDestino(2L);
        request.setValorTransferencia(BigDecimal.valueOf(150.00));

        TransferenciaAceitaResponse response = producer.enviarParaFila(request);

        assertNotNull(response);
        assertNotNull(response.getProtocolo());
        assertEquals("RECEBIDA", response.getStatus());

        ArgumentCaptor<TransferenciaMensagem> captor = ArgumentCaptor.forClass(TransferenciaMensagem.class);
        verify(rabbitTemplate).convertAndSend(eq("transferencia.exchange"), eq("transferencia.routing-key"), captor.capture());

        TransferenciaMensagem mensagemEnviada = captor.getValue();
        assertEquals(response.getProtocolo(), mensagemEnviada.getProtocolo());
        assertEquals(1L, mensagemEnviada.getIdContaOrigem());
        assertEquals(2L, mensagemEnviada.getIdContaDestino());
        assertEquals(BigDecimal.valueOf(150.00), mensagemEnviada.getValorTransferencia());
    }

    @Test
    void deveLancarExcecaoQuandoContasOrigemEDestinoForemIguais() {
        TransferenciaRequest request = new TransferenciaRequest();
        request.setIdContaOrigem(1L);
        request.setIdContaDestino(1L);
        request.setValorTransferencia(BigDecimal.valueOf(50.00));

        TransferenciaInvalidaException exception = assertThrows(
                TransferenciaInvalidaException.class,
                () -> producer.enviarParaFila(request)
        );

        assertEquals("A conta de origem e destino não podem ser iguais.", exception.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoValorForMenorOuIgualAZero() {
        TransferenciaRequest request = new TransferenciaRequest();
        request.setIdContaOrigem(1L);
        request.setIdContaDestino(2L);
        request.setValorTransferencia(BigDecimal.ZERO);

        assertThrows(
                TransferenciaInvalidaException.class,
                () -> producer.enviarParaFila(request)
        );
    }
}
