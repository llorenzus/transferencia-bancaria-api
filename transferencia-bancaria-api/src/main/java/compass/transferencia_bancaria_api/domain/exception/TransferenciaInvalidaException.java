package compass.transferencia_bancaria_api.domain.exception;

public class TransferenciaInvalidaException extends NegocioException{
    public TransferenciaInvalidaException(String message) {
        super(message);
    }
}
