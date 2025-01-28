package br.com.drkmatheus.exception;

public class OperationCancelledException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public OperationCancelledException(String mensagem) {
        super(mensagem);
    }
}
