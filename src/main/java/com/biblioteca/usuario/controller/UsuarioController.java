package com.biblioteca.usuario.controller;

import com.biblioteca.usuario.business.UsuarioService;
import com.biblioteca.usuario.business.dtos.*;
import com.biblioteca.usuario.infrastructure.security.SecurityConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuario")
@RequiredArgsConstructor
@Tag(name = "Usuário", description = "Microserviço de Usuário")
@SecurityRequirement(name = SecurityConfig.SECURITY_SCHEME)
public class UsuarioController {
    private final UsuarioService usuarioService;

    @PostMapping
    @Operation(
            summary = "Cadastrar usuário",
            description = "Cadastra um novo usuário no sistema da biblioteca."
    )
    public ResponseEntity<UsuarioResponseDTO> cadastrar(@Valid @RequestBody UsuarioRequestDTO request){
        UsuarioResponseDTO usuario = usuarioService.cadastrar(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(usuario);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login de usuário",
            description = "Login de usuário no sistema da biblioteca."
    )
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request){
        LoginResponseDTO response = usuarioService.login(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/email/{email}")
    @Operation(
            summary = "Consultar usuário",
            description = "Consulta de usuário por email no sistema da biblioteca."
    )
    public ResponseEntity<UsuarioResponseDTO> buscarPorEmail(@PathVariable String email, Authentication authentication){
        String emailAutenticado = authentication.getName();

        boolean administrador = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        UsuarioResponseDTO response = usuarioService.buscarPorEmail(email, emailAutenticado, administrador);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(
            summary = "Consultar meu perfil",
            description = "Consulta o próprio perfil no sistema da biblioteca."
    )
    public ResponseEntity<UsuarioResponseDTO> buscarMeuPerfil(Authentication authentication){
        String emailAutenticado = authentication.getName();

        UsuarioResponseDTO response = usuarioService.buscarMeuPerfil(emailAutenticado);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    @Operation(
            summary = "Atualizar meu perfil",
            description = "Atualiza o próprio perfil no sistema da biblioteca."
    )
    public ResponseEntity<UsuarioResponseDTO> atualizarMeuPerfil(@RequestBody @Valid UsuarioAtualizacaoDTO request, Authentication authentication){
        UsuarioResponseDTO response = usuarioService.atualizarMeuPerfil(authentication.getName(), request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{email}")
    @Operation(
            summary = "Excluir usuário",
            description = "Exclui o usuário no sistema da biblioteca."
    )
    public ResponseEntity<Void> excluir(@PathVariable String email, Authentication authentication){
        String emailAutenticado = authentication.getName();

        boolean administrador = authentication.getAuthorities()
                .stream()
                .anyMatch(autoridade ->
                        autoridade.getAuthority().equals("ROLE_ADMIN")
                );

        usuarioService.excluir(email, emailAutenticado, administrador);

        return ResponseEntity.noContent().build();
    }
}
