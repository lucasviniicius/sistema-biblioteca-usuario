package com.biblioteca.usuario.infrastructure.exception;

public class UsuarioNaoEncontradoException extends RuntimeException {
    public UsuarioNaoEncontradoException(String menssagem) {
        super(menssagem);
    }
}
