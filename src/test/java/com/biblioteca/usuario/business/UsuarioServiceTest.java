package com.biblioteca.usuario.business;

import com.biblioteca.usuario.business.UsuarioService;
import com.biblioteca.usuario.business.dtos.*;
import com.biblioteca.usuario.business.mapper.UsuarioMapper;
import com.biblioteca.usuario.infrastructure.entity.Usuario;
import com.biblioteca.usuario.infrastructure.enums.Roles;
import com.biblioteca.usuario.infrastructure.exception.UsuarioNaoEncontradoException;
import com.biblioteca.usuario.infrastructure.repository.UsuarioRepository;
import com.biblioteca.usuario.infrastructure.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void deveCadastrarUsuarioComSucesso() {
        UsuarioRequestDTO request = new UsuarioRequestDTO(
                "Lucas",
                "lucas@email.com",
                "senha123"
        );

        Usuario usuario = new Usuario();
        usuario.setNome("Lucas");
        usuario.setEmail("lucas@email.com");

        Usuario usuarioSalvo = new Usuario();
        usuarioSalvo.setId(1L);
        usuarioSalvo.setNome("Lucas");
        usuarioSalvo.setEmail("lucas@email.com");
        usuarioSalvo.setRole(Roles.USER);

        UsuarioResponseDTO respostaEsperada = new UsuarioResponseDTO(
                1L,
                "Lucas",
                "lucas@email.com",
                Roles.USER,
                null,
                null
        );

        when(usuarioRepository.existsByEmail("lucas@email.com"))
                .thenReturn(false);

        when(usuarioMapper.toEntity(request))
                .thenReturn(usuario);

        when(passwordEncoder.encode("senha123"))
                .thenReturn("senha-criptografada");

        when(usuarioRepository.save(usuario))
                .thenReturn(usuarioSalvo);

        when(usuarioMapper.toResponseDTO(usuarioSalvo))
                .thenReturn(respostaEsperada);

        UsuarioResponseDTO resultado = usuarioService.cadastrar(request);

        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        assertEquals("Lucas", resultado.nome());
        assertEquals("lucas@email.com", resultado.email());
        assertEquals(Roles.USER, resultado.role());

        verify(usuarioRepository).existsByEmail("lucas@email.com");
        verify(passwordEncoder).encode("senha123");
        verify(usuarioRepository).save(usuario);
        assertEquals("senha-criptografada", usuario.getSenha());
        assertEquals(Roles.USER, usuario.getRole());
    }

    @Test
    void deveLancarExcecaoQuandoEmailJaExiste() {
        UsuarioRequestDTO request = new UsuarioRequestDTO(
                "Lucas",
                "lucas@email.com",
                "senha123"
        );

        when(usuarioRepository.existsByEmail("lucas@email.com"))
                .thenReturn(true);

        IllegalArgumentException excecao = org.junit.jupiter.api.Assertions
                .assertThrows(
                        IllegalArgumentException.class,
                        () -> usuarioService.cadastrar(request)
                );

        assertEquals("Email já existe.", excecao.getMessage());

        verify(usuarioRepository).existsByEmail("lucas@email.com");
        verify(usuarioRepository, never()).save(any(Usuario.class));

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExiste() {
        when(usuarioRepository.findByEmail("inexistente@email.com"))
                .thenReturn(java.util.Optional.empty());

        UsuarioNaoEncontradoException excecao =
                org.junit.jupiter.api.Assertions.assertThrows(
                        UsuarioNaoEncontradoException.class,
                        () -> usuarioService.buscarMeuPerfil(
                                "inexistente@email.com"
                        )
                );

        assertEquals("Usuário não encontrado.", excecao.getMessage());

        verify(usuarioRepository).findByEmail("inexistente@email.com");
        verifyNoInteractions(usuarioMapper);
    }

    @Test
    void deveNegarConsultaDeOutroUsuarioQuandoNaoForAdministrador() {
        when(usuarioRepository.findByEmail("outro@email.com"))
                .thenReturn(java.util.Optional.of(new Usuario()));

        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.security.access.AccessDeniedException.class,
                () -> usuarioService.buscarPorEmail(
                        "outro@email.com",
                        "lucas@email.com",
                        false
                )
        );

        verifyNoInteractions(usuarioMapper);
    }

    @Test
    void deveNegarExclusaoDeOutroUsuarioQuandoNaoForAdministrador() {
        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.security.access.AccessDeniedException.class,
                () -> usuarioService.excluir(
                        "outro@email.com",
                        "lucas@email.com",
                        false
                )
        );

        verifyNoInteractions(usuarioRepository);
    }


    @Test
    void deveLancarExcecaoAoAtualizarPerfilDeUsuarioInexistente() {
        when(usuarioRepository.findByEmail("inexistente@email.com"))
                .thenReturn(java.util.Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(
                UsuarioNaoEncontradoException.class,
                () -> usuarioService.atualizarMeuPerfil(
                        "inexistente@email.com",
                        new UsuarioAtualizacaoDTO(
                                null,
                                null,
                                null
                        )
                )
        );

        verify(usuarioRepository).findByEmail("inexistente@email.com");
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verifyNoInteractions(usuarioMapper);
    }


    @Test
    void deveAtualizarNomeDoProprioPerfil() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Lucas");
        usuario.setEmail("lucas@email.com");
        usuario.setRole(Roles.USER);

        Usuario usuarioAtualizado = new Usuario();
        usuarioAtualizado.setId(1L);
        usuarioAtualizado.setNome("Lucas Vinicius");
        usuarioAtualizado.setEmail("lucas@email.com");
        usuarioAtualizado.setRole(Roles.USER);

        UsuarioAtualizacaoDTO request = new UsuarioAtualizacaoDTO(
                "Lucas Vinicius",
                null,
                null
        );

        UsuarioResponseDTO respostaEsperada = new UsuarioResponseDTO(
                1L,
                "Lucas Vinicius",
                "lucas@email.com",
                Roles.USER,
                null,
                null
        );

        when(usuarioRepository.findByEmail("lucas@email.com"))
                .thenReturn(java.util.Optional.of(usuario));

        when(usuarioRepository.save(usuario))
                .thenReturn(usuarioAtualizado);

        when(usuarioMapper.toResponseDTO(usuarioAtualizado))
                .thenReturn(respostaEsperada);

        UsuarioResponseDTO resultado = usuarioService.atualizarMeuPerfil(
                "lucas@email.com",
                request
        );

        assertEquals("Lucas Vinicius", resultado.nome());
        assertEquals("lucas@email.com", resultado.email());

        verify(usuarioRepository).save(usuario);
        verify(usuarioMapper).toResponseDTO(usuarioAtualizado);
    }


    @Test
    void deveRealizarLoginComSucesso() {
        LoginRequestDTO request = new LoginRequestDTO(
                "lucas@email.com",
                "senha123"
        );

        Authentication autenticacao =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        "lucas@email.com",
                        null
                );

        when(authenticationManager.authenticate(any(
                org.springframework.security.authentication.UsernamePasswordAuthenticationToken.class
        ))).thenReturn(autenticacao);

        when(jwtUtil.generateToken("lucas@email.com"))
                .thenReturn("token-jwt-teste");

        LoginResponseDTO resultado = usuarioService.login(request);

        assertNotNull(resultado);
        assertEquals("token-jwt-teste", resultado.token());
        assertEquals("Bearer", resultado.tipo());

        verify(authenticationManager).authenticate(any(
                org.springframework.security.authentication.UsernamePasswordAuthenticationToken.class
        ));

        verify(jwtUtil).generateToken("lucas@email.com");
    }


    @Test
    void deveFalharLoginQuandoCredenciaisSaoInvalidas() {
        LoginRequestDTO request = new LoginRequestDTO(
                "lucas@email.com",
                "senha-errada"
        );

        when(authenticationManager.authenticate(any(
                org.springframework.security.authentication.UsernamePasswordAuthenticationToken.class
        ))).thenThrow(
                new org.springframework.security.authentication.BadCredentialsException(
                        "Credenciais inválidas"
                )
        );

        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.security.authentication.BadCredentialsException.class,
                () -> usuarioService.login(request)
        );

        verify(authenticationManager).authenticate(any(
                org.springframework.security.authentication.UsernamePasswordAuthenticationToken.class
        ));

        verifyNoInteractions(jwtUtil);
    }


    @Test
    void deveExcluirPropriaContaComSucesso() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Lucas");
        usuario.setEmail("lucas@email.com");
        usuario.setRole(Roles.USER);

        when(usuarioRepository.findByEmail("lucas@email.com"))
                .thenReturn(java.util.Optional.of(usuario));

        usuarioService.excluir(
                "lucas@email.com",
                "lucas@email.com",
                false
        );

        verify(usuarioRepository).findByEmail("lucas@email.com");
        verify(usuarioRepository).delete(usuario);
    }


    @Test
    void devePermitirAdministradorExcluirOutroUsuario() {
        Usuario usuario = new Usuario();
        usuario.setId(2L);
        usuario.setNome("Outro usuário");
        usuario.setEmail("outro@email.com");
        usuario.setRole(Roles.USER);

        when(usuarioRepository.findByEmail("outro@email.com"))
                .thenReturn(java.util.Optional.of(usuario));

        usuarioService.excluir(
                "outro@email.com",
                "admin@email.com",
                true
        );

        verify(usuarioRepository).findByEmail("outro@email.com");
        verify(usuarioRepository).delete(usuario);
    }


    @Test
    void deveCodificarNovaSenhaAoAtualizarPerfil() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Lucas");
        usuario.setEmail("lucas@email.com");
        usuario.setRole(Roles.USER);
        usuario.setSenha("senha-antiga-codificada");

        Usuario usuarioAtualizado = new Usuario();
        usuarioAtualizado.setId(1L);
        usuarioAtualizado.setNome("Lucas");
        usuarioAtualizado.setEmail("lucas@email.com");
        usuarioAtualizado.setRole(Roles.USER);
        usuarioAtualizado.setSenha("nova-senha-codificada");

        UsuarioAtualizacaoDTO request = new UsuarioAtualizacaoDTO(
                null,
                null,
                "novaSenha123"
        );

        when(usuarioRepository.findByEmail("lucas@email.com"))
                .thenReturn(java.util.Optional.of(usuario));

        when(passwordEncoder.encode("novaSenha123"))
                .thenReturn("nova-senha-codificada");

        when(usuarioRepository.save(usuario))
                .thenReturn(usuarioAtualizado);

        when(usuarioMapper.toResponseDTO(usuarioAtualizado))
                .thenReturn(new UsuarioResponseDTO(
                        1L,
                        "Lucas",
                        "lucas@email.com",
                        Roles.USER,
                        null,
                        null
                ));

        UsuarioResponseDTO resultado = usuarioService.atualizarMeuPerfil(
                "lucas@email.com",
                request
        );

        assertEquals("Lucas", resultado.nome());

        verify(passwordEncoder).encode("novaSenha123");
        verify(usuarioRepository).save(usuario);
    }


    @Test
    void deveImpedirAtualizacaoParaEmailJaCadastrado() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Lucas");
        usuario.setEmail("lucas@email.com");
        usuario.setRole(Roles.USER);

        UsuarioAtualizacaoDTO request = new UsuarioAtualizacaoDTO(
                "outro@email.com",
                null,
                null
        );

        when(usuarioRepository.findByEmail("lucas@email.com"))
                .thenReturn(java.util.Optional.of(usuario));

        when(usuarioRepository.existsByEmail("outro@email.com"))
                .thenReturn(true);

        IllegalArgumentException excecao =
                org.junit.jupiter.api.Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> usuarioService.atualizarMeuPerfil(
                                "lucas@email.com",
                                request
                        )
                );

        assertEquals(
                "Este email já está cadastrado.",
                excecao.getMessage()
        );

        verify(usuarioRepository, never()).save(any(Usuario.class));
        verifyNoInteractions(usuarioMapper);
    }


    @Test
    void deveAtualizarEmailDoProprioPerfil() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Lucas");
        usuario.setEmail("lucas@email.com");
        usuario.setRole(Roles.USER);

        Usuario usuarioAtualizado = new Usuario();
        usuarioAtualizado.setId(1L);
        usuarioAtualizado.setNome("Lucas");
        usuarioAtualizado.setEmail("lucas.novo@email.com");
        usuarioAtualizado.setRole(Roles.USER);

        UsuarioAtualizacaoDTO request = new UsuarioAtualizacaoDTO(
                "lucas.novo@email.com",
                null,
                null
        );

        UsuarioResponseDTO respostaEsperada = new UsuarioResponseDTO(
                1L,
                "Lucas",
                "lucas.novo@email.com",
                Roles.USER,
                null,
                null
        );

        when(usuarioRepository.findByEmail("lucas@email.com"))
                .thenReturn(java.util.Optional.of(usuario));

        when(usuarioRepository.existsByEmail("lucas.novo@email.com"))
                .thenReturn(false);

        when(usuarioRepository.save(usuario))
                .thenReturn(usuarioAtualizado);

        when(usuarioMapper.toResponseDTO(usuarioAtualizado))
                .thenReturn(respostaEsperada);

        UsuarioResponseDTO resultado = usuarioService.atualizarMeuPerfil(
                "lucas@email.com",
                request
        );

        assertEquals("Lucas", resultado.nome());
        assertEquals("lucas.novo@email.com", resultado.email());

        verify(usuarioRepository).findByEmail("lucas@email.com");
        verify(usuarioRepository).existsByEmail("lucas.novo@email.com");
        verify(usuarioRepository).save(usuario);
        verify(usuarioMapper).toResponseDTO(usuarioAtualizado);
    }


    @Test
    void deveLancarExcecaoAoExcluirUsuarioInexistente() {
        when(usuarioRepository.findByEmail("inexistente@email.com"))
                .thenReturn(java.util.Optional.empty());

        UsuarioNaoEncontradoException excecao =
                org.junit.jupiter.api.Assertions.assertThrows(
                        UsuarioNaoEncontradoException.class,
                        () -> usuarioService.excluir(
                                "inexistente@email.com",
                                "admin@email.com",
                                true
                        )
                );

        assertEquals("Usuário não encontrado.", excecao.getMessage());

        verify(usuarioRepository).findByEmail("inexistente@email.com");

        verify(usuarioRepository, never()).delete(any(Usuario.class));
    }

}