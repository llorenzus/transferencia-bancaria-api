package compass.transferencia_bancaria_api.service;

import compass.transferencia_bancaria_api.controller.dto.TransferenciaRequest;
import compass.transferencia_bancaria_api.domain.exception.ContaNaoEncontradaException;
import compass.transferencia_bancaria_api.domain.exception.SaldoInsuficienteException;
import compass.transferencia_bancaria_api.domain.exception.TransferenciaInvalidaException;
import compass.transferencia_bancaria_api.domain.model.Conta;
import compass.transferencia_bancaria_api.repository.ContaRepository;
import compass.transferencia_bancaria_api.repository.TransacaoRepository;
import compass.transferencia_bancaria_api.service.TransacaoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransacaoServiceTest {
    @Mock
    private ContaRepository contaRepository;
    @Mock
    private TransacaoRepository transacaoRepository;

    @InjectMocks
    private TransacaoService transacaoService;

    @Test
    void deveLancarExcecaoQuandoContaOrigemNaoExiste(){
        TransferenciaRequest request = new TransferenciaRequest();
        request.setIdContaOrigem(1L);
        request.setIdContaDestino(2L);
        request.setValorTransferencia(BigDecimal.TEN);

        when(contaRepository.findByIdForUpdate(1L)).thenReturn(Optional.empty());

        assertThrows(ContaNaoEncontradaException.class, () -> {
            transacaoService.realizarTransferencia(request);
        });
    }

    @Test
    void deveLancarExcecaoQuandoContaDestinoNaoExiste(){
        TransferenciaRequest request = new TransferenciaRequest();
        request.setIdContaOrigem(1L);
        request.setIdContaDestino(2L);
        request.setValorTransferencia(BigDecimal.TEN);

        Conta conta = new Conta(1L, "Conta Teste", BigDecimal.ZERO);

        when(contaRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(conta));

        when(contaRepository.findByIdForUpdate(2L)).thenReturn(Optional.empty());

        assertThrows(ContaNaoEncontradaException.class, () -> {
            transacaoService.realizarTransferencia(request);
        });
    }

    @Test
    void deveLancarExcecaoQuandoContaNaoPossuiSaldo(){
        TransferenciaRequest request = new TransferenciaRequest();
        request.setIdContaOrigem(1L);
        request.setIdContaDestino(2L);
        request.setValorTransferencia(BigDecimal.TEN);
        Conta origem = new Conta(1L, "Conta Teste Origem", BigDecimal.ZERO);
        Conta destino = new Conta(2L, "Conta Teste Destino", BigDecimal.TEN);

        when(contaRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(origem));
        when(contaRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(destino));

        assertThrows(SaldoInsuficienteException.class, () -> {
            transacaoService.realizarTransferencia(request);
        });
    }

    @Test
    void deveRealizarTransferenciaComSucesso(){
        TransferenciaRequest request = new TransferenciaRequest();
        request.setIdContaOrigem(1L);
        request.setIdContaDestino(2L);
        request.setValorTransferencia(BigDecimal.TEN);
        Conta origem = new Conta(1L, "Conta Teste Origem", new BigDecimal(100));
        Conta destino = new Conta(2L, "Conta Teste Destino", new BigDecimal(50));

        when(contaRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(origem));

        when(contaRepository.findByIdForUpdate(2L))
                .thenReturn(Optional.of(destino));

        when(contaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        transacaoService.realizarTransferencia(request);

        assertEquals(new BigDecimal("90"), origem.getSaldo());
        assertEquals(new BigDecimal("60"), destino.getSaldo());
    }

    @Test
    void deveLancarExcecaoQuandoContaOrigemIgualDestino(){
        TransferenciaRequest request = new TransferenciaRequest();
        request.setIdContaOrigem(1L);
        request.setIdContaDestino(1L);
        request.setValorTransferencia(BigDecimal.TEN);

        assertThrows(TransferenciaInvalidaException.class, () -> {
            transacaoService.realizarTransferencia(request);
        });
    }

    @Test
    void devePermitirTransferenciaQuandoSaldoIgualValor() {

        TransferenciaRequest request = new TransferenciaRequest();
        request.setIdContaOrigem(1L);
        request.setIdContaDestino(2L);
        request.setValorTransferencia(BigDecimal.TEN);

        Conta origem = new Conta(1L, "Origem", BigDecimal.TEN);
        Conta destino = new Conta(2L, "Destino", BigDecimal.ZERO);

        when(contaRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(origem));

        when(contaRepository.findByIdForUpdate(2L))
                .thenReturn(Optional.of(destino));

        when(contaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        transacaoService.realizarTransferencia(request);

        assertEquals(BigDecimal.ZERO, origem.getSaldo());
    }

    @Test
    void deveLancarExcecaoQuandoValorForZeroOuNegativo() {

        TransferenciaRequest request = new TransferenciaRequest();
        request.setIdContaOrigem(1L);
        request.setIdContaDestino(2L);
        request.setValorTransferencia(BigDecimal.ZERO);

        assertThrows(TransferenciaInvalidaException.class, () -> {
            transacaoService.realizarTransferencia(request);
        });
    }
}
