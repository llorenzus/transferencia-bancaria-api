package compass.transferencia_bancaria_api.service;

import compass.transferencia_bancaria_api.controller.dto.TransferenciaAceitaResponse;
import compass.transferencia_bancaria_api.controller.dto.TransferenciaMensagem;
import compass.transferencia_bancaria_api.controller.dto.TransferenciaRequest;
import compass.transferencia_bancaria_api.domain.exception.TransferenciaInvalidaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TransferenciaProducer {

    private static final Logger log = LoggerFactory.getLogger(TransferenciaProducer.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange.transferencia:transferencia.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.routing-key.transferencia:transferencia.routing-key}")
    private String routingKey;

    public TransferenciaProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public TransferenciaAceitaResponse enviarParaFila(TransferenciaRequest request) {
        if (request.getIdContaOrigem().equals(request.getIdContaDestino())) {
            throw new TransferenciaInvalidaException("A conta de origem e destino não podem ser iguais.");
        }

        if (request.getValorTransferencia() == null || request.getValorTransferencia().compareTo(BigDecimal.ZERO) <= 0) {
            throw new TransferenciaInvalidaException("Valor deve ser maior que zero.");
        }

        String protocolo = UUID.randomUUID().toString();
        LocalDateTime agora = LocalDateTime.now();

        TransferenciaMensagem mensagem = new TransferenciaMensagem(
                protocolo,
                request.getIdContaOrigem(),
                request.getIdContaDestino(),
                request.getValorTransferencia(),
                agora
        );

        log.info("[FILA] Publicando solicitação de transferência na fila. Protocolo: {}, Origem: {}, Destino: {}, Valor: {}",
                protocolo, request.getIdContaOrigem(), request.getIdContaDestino(), request.getValorTransferencia());

        rabbitTemplate.convertAndSend(exchangeName, routingKey, mensagem);

        return new TransferenciaAceitaResponse(
                protocolo,
                "RECEBIDA",
                "Transferência recebida com sucesso e enfileirada para processamento.",
                agora
        );
    }
}
