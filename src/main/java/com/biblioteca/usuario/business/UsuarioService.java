package com.biblioteca.usuario.business;

import com.biblioteca.usuario.business.dtos.LoginRequestDTO;
import com.biblioteca.usuario.business.dtos.LoginResponseDTO;
import com.biblioteca.usuario.business.dtos.UsuarioRequestDTO;
import com.biblioteca.usuario.business.dtos.UsuarioResponseDTO;
import com.biblioteca.usuario.business.mapper.UsuarioMapper;
import com.biblioteca.usuario.infrastructure.entity.Usuario;
import com.biblioteca.usuario.infrastructure.enums.Roles;
import com.biblioteca.usuario.infrastructure.repository.UsuarioRepository;
import com.biblioteca.usuario.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
}
