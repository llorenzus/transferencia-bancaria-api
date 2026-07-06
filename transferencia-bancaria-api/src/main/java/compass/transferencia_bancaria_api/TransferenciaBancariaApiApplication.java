package compass.transferencia_bancaria_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class TransferenciaBancariaApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransferenciaBancariaApiApplication.class, args);
	}

}