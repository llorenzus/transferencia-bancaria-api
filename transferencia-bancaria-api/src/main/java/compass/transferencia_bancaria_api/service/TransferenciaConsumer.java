package compass.transferencia_bancaria_api.service;

import compass.transferencia_bancaria_api.controller.dto.TransferenciaMensagem;
import compass.transferencia_bancaria_api.domain.exception.NegocioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class TransferenciaConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransferenciaConsumer.class);

    private final TransacaoService transacaoService;

    public TransferenciaConsumer(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.transferencia:transferencia.queue}")
    public void processarMensagem(TransferenciaMensagem mensagem) {
        log.info("[CONSUMER] Mensagem recebida da fila. Protocolo: {}, Origem: {}, Destino: {}, Valor: {}",
                mensagem.getProtocolo(), mensagem.getIdContaOrigem(), mensagem.getIdContaDestino(), mensagem.getValorTransferencia());

        try {
            transacaoService.processarTransferenciaEnfileirada(mensagem);
            log.info("[CONSUMER] Transferência processada com sucesso. Protocolo: {}", mensagem.getProtocolo());
        } catch (NegocioException e) {
            log.error("[CONSUMER - ERRO DE NEGÓCIO] Falha irrecuperável ao processar transferência (Protocolo: {}). Encaminhando para DLQ: {}",
                    mensagem.getProtocolo(), e.getMessage());
            // Erros de negócio não devem ser reprocessados pelo retry; são encaminhados direto para a Dead Letter Queue (DLQ)
            throw new AmqpRejectAndDontRequeueException("Erro de negócio irrecuperável: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[CONSUMER - ERRO TRANSITÓRIO/SISTEMA] Falha ao processar transferência (Protocolo: {}). Tentativa de retry ou DLQ: {}",
                    mensagem.getProtocolo(), e.getMessage(), e);
            // Erros transitórios são relançados para acionar a política de retry configurada
            throw e;
        }
    }
}
