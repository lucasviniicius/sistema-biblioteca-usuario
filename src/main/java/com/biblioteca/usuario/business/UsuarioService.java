package com.biblioteca.usuario.business;

import com.biblioteca.usuario.business.dtos.*;
import com.biblioteca.usuario.business.mapper.UsuarioMapper;
import com.biblioteca.usuario.infrastructure.entity.Usuario;
import com.biblioteca.usuario.infrastructure.enums.Roles;
import com.biblioteca.usuario.infrastructure.exception.UsuarioNaoEncontradoException;
import com.biblioteca.usuario.infrastructure.repository.UsuarioRepository;
import com.biblioteca.usuario.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper usuarioMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public UsuarioResponseDTO cadastrar(UsuarioRequestDTO request){
        if(usuarioRepository.existsByEmail(request.email())){
            throw new IllegalArgumentException(
                    "Email já existe."
            );
        }

        Usuario usuario = usuarioMapper.toEntity(request);

        usuario.setSenha(passwordEncoder.encode(request.senha()));
        usuario.setRole(Roles.USER);

        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        return usuarioMapper.toResponseDTO(usuarioSalvo);
    }

    public LoginResponseDTO login(LoginRequestDTO request){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.senha()
                )
        );

        String token = jwtUtil.generateToken(
                authentication.getName()
        );

        return new LoginResponseDTO(token, "Bearer");
    }

    public UsuarioResponseDTO buscarPorEmail(String email, String emailAutenticado, boolean administrador) {
        Usuario usuario = usuarioRepository.findByEmail(email).
                orElseThrow(() -> new UsuarioNaoEncontradoException(
                        "Usuário não encontrado."
                ));

        if(!administrador && !email.equalsIgnoreCase(emailAutenticado)){
            throw new AccessDeniedException(
                    "Você não tem permissão para consultar esse usuário."
            );
        }

        return usuarioMapper.toResponseDTO(usuario);
    }

    public UsuarioResponseDTO buscarMeuPerfil(String emailAutenticado){
        Usuario usuario = usuarioRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(
                "Usuário não encontrado."
        ));

        return usuarioMapper.toResponseDTO(usuario);
    }

    public UsuarioResponseDTO atualizarMeuPerfil(String emailAutenticado, UsuarioAtualizacaoDTO request){
        Usuario usuario = usuarioRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(
                        "Usuário não encontrado."
                ));

        if(request.nome() != null){
            usuario.setNome(request.nome());
        }

        if(request.email() != null){
            boolean emailEmUso = usuarioRepository.existsByEmail(request.email())
                    && !usuario.getEmail().equalsIgnoreCase(request.email());

            if(emailEmUso){
                throw new IllegalArgumentException(
                        "Este email já está cadastrado."
                );
            }

            usuario.setEmail(request.email());
        }

        if(request.senha() != null){
            usuario.setSenha(passwordEncoder.encode(request.senha()));
        }

        Usuario usuarioAtualizado = usuarioRepository.save(usuario);

        return usuarioMapper.toResponseDTO(usuarioAtualizado);
    }

    public void excluir(String email, String emailAutenticado, boolean administrador){
        if(!administrador && !email.equalsIgnoreCase(emailAutenticado)){
            throw new AccessDeniedException(
                    "Você não tem permissão para excluir outro usuário."
            );
        }

        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(
                () -> new UsuarioNaoEncontradoException("Usuário não encontrado.")
        );

        usuarioRepository.delete(usuario);
    }
}
