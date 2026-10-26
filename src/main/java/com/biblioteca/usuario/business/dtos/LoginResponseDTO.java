package com.biblioteca.usuario.business.dtos;

public record LoginResponseDTO(
        String token,
        String tipo
) {
}
