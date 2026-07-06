package compass.transferencia_bancaria_api.config;

import compass.transferencia_bancaria_api.domain.model.Conta;
import compass.transferencia_bancaria_api.repository.ContaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;
import java.util.List;

@Configuration
public class CargaDadosConfig {

    @Bean
    CommandLineRunner carregarContasIniciais(ContaRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.saveAll(List.of(
                        new Conta(null, "Vinicius Junior", new BigDecimal("5000.00")),
                        new Conta(null, "Gabriel Magalhães", new BigDecimal("1500.00")),
                        new Conta(null, "Alisson Becker", new BigDecimal("3000.00")),
                        new Conta(null, "Lucas Paquetá", new BigDecimal("4000.00"))
                ));
                System.out.println("[CARGA INICIAL] Contas de teste criadas com sucesso!");
            }
        };
    }
}