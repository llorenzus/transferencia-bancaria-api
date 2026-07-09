package compass.transferencia_bancaria_api.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotificacaoService {
    @Async
    public void enviarNotificacao(String cliente, String mensagem) {
        // Simulando o envio de notificação
        System.out.println("[NOTIFICAÇÃO ASSÍNCRONA] Enviado para " + cliente + ": " + mensagem);
    }
}
