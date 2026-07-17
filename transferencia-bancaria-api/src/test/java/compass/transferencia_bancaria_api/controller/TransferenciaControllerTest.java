package compass.transferencia_bancaria_api.controller;

import compass.transferencia_bancaria_api.controller.dto.TransacaoResponse;
import compass.transferencia_bancaria_api.controller.dto.TransferenciaRequest;
import compass.transferencia_bancaria_api.domain.model.Transacao;
import compass.transferencia_bancaria_api.service.TransacaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransferenciaController.class)
class TransferenciaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransacaoService transacaoService;

    @Autowired
    private ObjectMapper objectMapper;

    // ----------------------------
    // POST /transacoes - sucesso
    // ----------------------------
    @Test
    void deveRealizarTransferenciaComSucesso() throws Exception {

        doNothing().when(transacaoService)
                .realizarTransferencia(any(TransferenciaRequest.class));

        TransferenciaRequest request = new TransferenciaRequest();
        request.setIdContaOrigem(1L);
        request.setIdContaDestino(2L);
        request.setValorTransferencia(BigDecimal.TEN);

        mockMvc.perform(post("/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Transferência concluída com sucesso."));
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
    // POST - erro de negócio (422 via service)
    // ----------------------------
    @Test
    void deveRetornarUnprocessableEntityQuandoServicoLancaTransferenciaInvalida() throws Exception {

        TransferenciaRequest request = new TransferenciaRequest();
        request.setIdContaOrigem(1L);
        request.setIdContaDestino(2L);
        request.setValorTransferencia(BigDecimal.TEN);

        org.mockito.Mockito.doThrow(new compass.transferencia_bancaria_api.domain.exception.TransferenciaInvalidaException("Erro de negócio"))
                .when(transacaoService).realizarTransferencia(any());

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
}