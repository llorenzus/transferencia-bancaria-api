package compass.transferencia_bancaria_api.domain.exception;

public class SaldoInsuficienteException extends NegocioException{
    public SaldoInsuficienteException(Long id) {
        super("Conta com o ID " + id + " não possui saldo suficiente para realizar a operação.");
    }
}
