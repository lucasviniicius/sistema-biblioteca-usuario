package com.biblioteca.usuario.business.dtos;

import com.biblioteca.usuario.infrastructure.enums.Roles;

import java.time.LocalDateTime;

public record UsuarioResponseDTO(
        Long id,
        String nome,
        String email,
        Roles role,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizao
) {}
