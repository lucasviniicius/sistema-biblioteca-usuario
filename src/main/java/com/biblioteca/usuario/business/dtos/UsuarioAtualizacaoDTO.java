package com.biblioteca.usuario.business.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioAtualizacaoDTO(
        @Email(message = "Informe um email válido.")
        String email,

        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        String nome,

        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres.")
        String senha
){
}
