package compass.transferencia_bancaria_api.service;

import compass.transferencia_bancaria_api.controller.dto.TransacaoResponse;
import compass.transferencia_bancaria_api.controller.dto.TransferenciaMensagem;
import compass.transferencia_bancaria_api.controller.dto.TransferenciaRequest;
import compass.transferencia_bancaria_api.domain.exception.ContaNaoEncontradaException;
import compass.transferencia_bancaria_api.domain.exception.TransferenciaInvalidaException;
import compass.transferencia_bancaria_api.domain.model.Conta;
import compass.transferencia_bancaria_api.domain.model.Transacao;
import compass.transferencia_bancaria_api.repository.ContaRepository;
import compass.transferencia_bancaria_api.repository.TransacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TransacaoService {

    private static final Logger log = LoggerFactory.getLogger(TransacaoService.class);

    private final ContaRepository contaRepository;
    private final TransacaoRepository transacaoRepository;
    private final NotificacaoService notificacaoService;

    public TransacaoService(ContaRepository contaRepository, TransacaoRepository transacaoRepository, NotificacaoService notificacaoService) {
        this.contaRepository = contaRepository;
        this.transacaoRepository = transacaoRepository;
        this.notificacaoService = notificacaoService;
    }

    @Transactional
    public void processarTransferenciaEnfileirada(TransferenciaMensagem mensagem) {
        log.info("[CONSUMER] Processando transferência da fila. Protocolo: {}", mensagem.getProtocolo());
        realizarTransferenciaInterna(
                mensagem.getProtocolo(),
                mensagem.getIdContaOrigem(),
                mensagem.getIdContaDestino(),
                mensagem.getValorTransferencia()
        );
    }

    @Transactional
    public void realizarTransferencia(TransferenciaRequest request) {
        realizarTransferenciaInterna(
                null,
                request.getIdContaOrigem(),
                request.getIdContaDestino(),
                request.getValorTransferencia()
        );
    }

    private void realizarTransferenciaInterna(String protocolo, Long idContaOrigem, Long idContaDestino, BigDecimal valor) {
        if (idContaOrigem.equals(idContaDestino)) {
            throw new TransferenciaInvalidaException("A conta de origem e destino não podem ser iguais.");
        }

        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new TransferenciaInvalidaException("Valor deve ser maior que zero.");
        }

        // Ordenação de IDs para mitigar riscos de Deadlock em alta concorrência
        Long primeiroId = Math.min(idContaOrigem, idContaDestino);
        Long segundoId = Math.max(idContaOrigem, idContaDestino);

        Conta primeiraConta = contaRepository.findByIdForUpdate(primeiroId)
                .orElseThrow(() -> new ContaNaoEncontradaException(primeiroId));
        Conta segundaConta = contaRepository.findByIdForUpdate(segundoId)
                .orElseThrow(() -> new ContaNaoEncontradaException(segundoId));

        Conta origem = primeiraConta.getId().equals(idContaOrigem) ? primeiraConta : segundaConta;
        Conta destino = primeiraConta.getId().equals(idContaDestino) ? primeiraConta : segundaConta;

        // Executa o débito e crédito
        origem.debitar(valor);
        destino.creditar(valor);

        contaRepository.save(origem);
        contaRepository.save(destino);

        // Salva o registro da movimentação financeira
        Transacao transacao = new Transacao(protocolo, origem, destino, LocalDateTime.now(), valor);
        transacaoRepository.save(transacao);

        log.info("[SUCESSO] Transferência concluída. Protocolo: {}, Origem: {}, Destino: {}, Valor: {}",
                protocolo, idContaOrigem, idContaDestino, valor);

        // Dispara as notificações de forma assíncrona apenas após o commit bem-sucedido
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    enviarNotificacoes(origem.getNome(), destino.getNome(), valor);
                }
            });
        } else {
            enviarNotificacoes(origem.getNome(), destino.getNome(), valor);
        }
    }

    private void enviarNotificacoes(String nomeOrigem, String nomeDestino, BigDecimal valor) {
        String mensagemNotificacaoContaOrigem = "Sua transferência de R$ " + valor + " foi realizada com sucesso.";
        this.notificacaoService.enviarNotificacao(nomeOrigem, mensagemNotificacaoContaOrigem);
        String mensagemNotificacaoContaDestino = "Você recebeu uma transferência de R$ " + valor + ".";
        this.notificacaoService.enviarNotificacao(nomeDestino, mensagemNotificacaoContaDestino);
    }

    public List<TransacaoResponse> listarMovimentacoes(Long origemId, Long destinoId) {
        return transacaoRepository.findByOrigemIdOrDestinoId(origemId, destinoId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Optional<TransacaoResponse> buscarPorProtocolo(String protocolo) {
        return transacaoRepository.findByProtocolo(protocolo)
                .map(this::toResponse);
    }

    private TransacaoResponse toResponse(Transacao t) {
        return new TransacaoResponse(
                t.getId(),
                t.getProtocolo(),
                t.getOrigem().getId(),
                t.getOrigem().getNome(),
                t.getDestino().getId(),
                t.getDestino().getNome(),
                t.getDataHora(),
                t.getValor()
        );
    }
}
