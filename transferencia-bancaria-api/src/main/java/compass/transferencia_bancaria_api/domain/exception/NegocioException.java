package compass.transferencia_bancaria_api.domain.exception;

public class NegocioException extends RuntimeException {
    public NegocioException(String message){
        super(message);
    }
}
