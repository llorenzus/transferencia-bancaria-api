package compass.transferencia_bancaria_api.service;

import compass.transferencia_bancaria_api.controller.dto.TransferenciaRequest;
import compass.transferencia_bancaria_api.domain.exception.ContaNaoEncontradaException;
import compass.transferencia_bancaria_api.domain.exception.TransferenciaInvalidaException;
import compass.transferencia_bancaria_api.domain.model.Conta;
import compass.transferencia_bancaria_api.domain.model.Transacao;
import compass.transferencia_bancaria_api.repository.ContaRepository;
import compass.transferencia_bancaria_api.repository.TransacaoRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransacaoService {

    private final ContaRepository contaRepository;
    private final TransacaoRepository transacaoRepository;
    private final NotificacaoService notificacaoService;

    public TransacaoService(ContaRepository contaRepository, TransacaoRepository transacaoRepository, NotificacaoService notificacaoService) {
        this.contaRepository = contaRepository;
        this.transacaoRepository = transacaoRepository;
        this.notificacaoService = notificacaoService;
    }

    @Transactional
    public void realizarTransferencia(TransferenciaRequest request) {
        if (request.getIdContaOrigem().equals(request.getIdContaDestino())) {
            throw new TransferenciaInvalidaException("A conta de origem e destino não podem ser iguais.");
        }

        if(request.getValorTransferencia().compareTo(BigDecimal.ONE) <= 0){
            throw new TransferenciaInvalidaException("Valor deve ser maior que zero.");
        }
        // Ordenação de IDs para mitigar riscos de Deadlock em alta concorrência
        Long primeiroId = Math.min(request.getIdContaOrigem(), request.getIdContaDestino());
        Long segundoId = Math.max(request.getIdContaOrigem(), request.getIdContaDestino());

        Conta primeiraConta = contaRepository.findByIdForUpdate(primeiroId)
                .orElseThrow(() -> new ContaNaoEncontradaException(primeiroId));
        Conta segundaConta = contaRepository.findByIdForUpdate(segundoId)
                .orElseThrow(() -> new ContaNaoEncontradaException(segundoId));

        Conta origem = primeiraConta.getId().equals(request.getIdContaOrigem()) ? primeiraConta : segundaConta;
        Conta destino = primeiraConta.getId().equals(request.getIdContaDestino()) ? primeiraConta : segundaConta;

        // Executa o débito e crédito
        origem.debitar(request.getValorTransferencia());
        destino.creditar(request.getValorTransferencia());

        contaRepository.save(origem);
        contaRepository.save(destino);

        // Salva o registro da movimentação financeira
        Transacao transacao = new Transacao(origem, destino, LocalDateTime.now(), request.getValorTransferencia());
        transacaoRepository.save(transacao);

        // Dispara as notificações de forma assíncrona
        String mensagemNotificacaoContaOrigem = "Sua transferência de R$ " + request.getValorTransferencia() + " foi realizada com sucesso.";
        this.notificacaoService.enviarNotificacao(origem.getNome(), mensagemNotificacaoContaOrigem);
        String mensagemNotificacaoContaDestino = "Você recebeu uma transferência de R$ " + request.getValorTransferencia() + ".";
        this.notificacaoService.enviarNotificacao(destino.getNome(), mensagemNotificacaoContaDestino);
    }

    public List<Transacao> listarMovimentacoes(Long origemId, Long destinoId) {
        return transacaoRepository.findByOrigemIdOrDestinoId(origemId, destinoId);
    }
}