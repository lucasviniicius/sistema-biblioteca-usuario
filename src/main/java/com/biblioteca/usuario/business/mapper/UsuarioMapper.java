package com.biblioteca.usuario.business.mapper;

import com.biblioteca.usuario.business.dtos.UsuarioRequestDTO;
import com.biblioteca.usuario.business.dtos.UsuarioResponseDTO;
import com.biblioteca.usuario.infrastructure.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    Usuario toEntity(UsuarioRequestDTO request);

    UsuarioResponseDTO toResponseDTO(Usuario usuario);
}
