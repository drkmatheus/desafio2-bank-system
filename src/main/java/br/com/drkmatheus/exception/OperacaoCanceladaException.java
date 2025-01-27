package br.com.drkmatheus.exception;

public class OperacaoCanceladaException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public OperacaoCanceladaException(String mensagem) {
        super(mensagem);
    }
}
