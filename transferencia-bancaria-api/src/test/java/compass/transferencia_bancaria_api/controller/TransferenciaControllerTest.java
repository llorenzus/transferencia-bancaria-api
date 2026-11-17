package compass.transferencia_bancaria_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import compass.transferencia_bancaria_api.controller.dto.TransacaoResponse;
import compass.transferencia_bancaria_api.controller.dto.TransferenciaAceitaResponse;
import compass.transferencia_bancaria_api.controller.dto.TransferenciaRequest;
import compass.transferencia_bancaria_api.domain.exception.TransferenciaInvalidaException;
import compass.transferencia_bancaria_api.service.TransacaoService;
import compass.transferencia_bancaria_api.service.TransferenciaProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransferenciaController.class)
class TransferenciaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransacaoService transacaoService;

    @MockBean
    private TransferenciaProducer transferenciaProducer;

    @Autowired
    private ObjectMapper objectMapper;

    // ----------------------------
    // POST /transacoes - sucesso (202 Accepted)
    // ----------------------------
    @Test
    void deveEnfileirarTransferenciaComSucesso() throws Exception {
        TransferenciaAceitaResponse response = new TransferenciaAceitaResponse(
                "abc-123-uuid",
                "RECEBIDA",
                "Transferência recebida com sucesso e enfileirada para processamento.",
                LocalDateTime.now()
        );

        when(transferenciaProducer.enviarParaFila(any(TransferenciaRequest.class)))
                .thenReturn(response);

        TransferenciaRequest request = new TransferenciaRequest();
        request.setIdContaOrigem(1L);
        request.setIdContaDestino(2L);
        request.setValorTransferencia(BigDecimal.TEN);

        mockMvc.perform(post("/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.protocolo").value("abc-123-uuid"))
                .andExpect(jsonPath("$.status").value("RECEBIDA"))
                .andExpect(jsonPath("$.mensagem").value("Transferência recebida com sucesso e enfileirada para processamento."));
    }

    // ----------------------------
    // POST - validação @Valid (400)
    // ----------------------------
    @Test
    void deveRetornarBadRequestQuandoRequestInvalido() throws Exception {
        String jsonInvalido = """
        {
          "idContaOrigem": null,
          "idContaDestino": 2,
          "valorTransferencia": 100
        }
        """;

        mockMvc.perform(post("/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest());
    }

    // ----------------------------
    // POST - erro de negócio (422 via producer)
    // ----------------------------
    @Test
    void deveRetornarUnprocessableEntityQuandoProdutorLancaTransferenciaInvalida() throws Exception {
        TransferenciaRequest request = new TransferenciaRequest();
        request.setIdContaOrigem(1L);
        request.setIdContaDestino(2L);
        request.setValorTransferencia(BigDecimal.TEN);

        when(transferenciaProducer.enviarParaFila(any()))
                .thenThrow(new TransferenciaInvalidaException("Erro de negócio"));

        mockMvc.perform(post("/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Erro de negócio"));
    }

    // ----------------------------
    // GET /historico/{contaId}
    // ----------------------------
    @Test
    void deveRetornarHistoricoDaConta() throws Exception {
        TransacaoResponse t1 = new TransacaoResponse();
        TransacaoResponse t2 = new TransacaoResponse();

        when(transacaoService.listarMovimentacoes(1L, 1L))
                .thenReturn(List.of(t1, t2));

        mockMvc.perform(get("/transacoes/historico/1"))
                .andExpect(status().isOk());
    }

    // ----------------------------
    // GET /transacoes/protocolo/{protocolo}
    // ----------------------------
    @Test
    void deveRetornarTransacaoPorProtocoloQuandoEncontrado() throws Exception {
        TransacaoResponse transacao = new TransacaoResponse(
                1L, "abc-123-uuid", 1L, "Origem", 2L, "Destino", LocalDateTime.now(), BigDecimal.TEN
        );

        when(transacaoService.buscarPorProtocolo("abc-123-uuid"))
                .thenReturn(Optional.of(transacao));

        mockMvc.perform(get("/transacoes/protocolo/abc-123-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.protocolo").value("abc-123-uuid"));
    }

    @Test
    void deveRetornarNotFoundQuandoProtocoloNaoEncontrado() throws Exception {
        when(transacaoService.buscarPorProtocolo("inexistente"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/transacoes/protocolo/inexistente"))
                .andExpect(status().isNotFound());
    }
}
