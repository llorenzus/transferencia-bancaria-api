package compass.transferencia_bancaria_api.domain.exception;

public class ContaNaoEncontradaException extends NegocioException{
    public ContaNaoEncontradaException(Long id) {
        super("Conta com o ID " + id + " não foi encontrada.");
    }
}
