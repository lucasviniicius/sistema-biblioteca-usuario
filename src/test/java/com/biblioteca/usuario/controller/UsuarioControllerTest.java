
package com.biblioteca.usuario.controller;

import com.biblioteca.usuario.business.UsuarioService;
import com.biblioteca.usuario.business.dtos.LoginRequestDTO;
import com.biblioteca.usuario.business.dtos.LoginResponseDTO;
import com.biblioteca.usuario.business.dtos.UsuarioAtualizacaoDTO;
import com.biblioteca.usuario.business.dtos.UsuarioRequestDTO;
import com.biblioteca.usuario.business.dtos.UsuarioResponseDTO;
import com.biblioteca.usuario.infrastructure.enums.Roles;
import com.biblioteca.usuario.infrastructure.security.JwtUtil;
import com.biblioteca.usuario.infrastructure.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
@Import(SecurityConfig.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource;

    private UsuarioResponseDTO criarUsuarioEsperado() {
        return new UsuarioResponseDTO(
                1L,
                "Lucas",
                "lucas@email.com",
                Roles.USER,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    void deveCadastrarUsuarioERetornarCreated() throws Exception {
        UsuarioRequestDTO request = new UsuarioRequestDTO(
                "Lucas",
                "lucas@email.com",
                "senha123"
        );

        when(usuarioService.cadastrar(any(UsuarioRequestDTO.class)))
                .thenReturn(criarUsuarioEsperado());

        mockMvc.perform(post("/usuario")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Lucas"))
                .andExpect(jsonPath("$.email").value("lucas@email.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void deveRetornarBadRequestQuandoCadastroForInvalido() throws Exception {
        UsuarioRequestDTO request = new UsuarioRequestDTO(
                "",
                "email-invalido",
                "123"
        );

        mockMvc.perform(post("/usuario")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRealizarLoginERetornarOk() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO(
                "lucas@email.com",
                "senha123"
        );

        LoginResponseDTO respostaEsperada =
                new LoginResponseDTO("token-jwt-teste", "Bearer");

        when(usuarioService.login(any(LoginRequestDTO.class)))
                .thenReturn(respostaEsperada);

        mockMvc.perform(post("/usuario/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-jwt-teste"))
                .andExpect(jsonPath("$.tipo").value("Bearer"));
    }

    @Test
    void deveRetornarUnauthorizedQuandoLoginForInvalido() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO(
                "lucas@email.com",
                "senha-errada"
        );

        doThrow(new org.springframework.security.authentication.BadCredentialsException(
                "E-mail ou senha inválidos."
        )).when(usuarioService).login(any(LoginRequestDTO.class));

        mockMvc.perform(post("/usuario/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornarUnauthorizedQuandoNaoEstiverAutenticado() throws Exception {
        mockMvc.perform(get("/usuario/me"))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @Test
    void deveBuscarMeuPerfilERetornarOk() throws Exception {
        when(usuarioService.buscarMeuPerfil("lucas@email.com"))
                .thenReturn(criarUsuarioEsperado());

        mockMvc.perform(get("/usuario/me")
                        .with(user("lucas@email.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("lucas@email.com"));
    }

    @Test
    void deveAtualizarMeuPerfilERetornarOk() throws Exception {
        UsuarioAtualizacaoDTO request = new UsuarioAtualizacaoDTO(
                "lucas@email.com",
                "Lucas Atualizado",
                "novaSenha123"
        );

        UsuarioResponseDTO usuarioAtualizado = new UsuarioResponseDTO(
                1L,
                "Lucas Atualizado",
                "lucas@email.com",
                Roles.USER,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(usuarioService.atualizarMeuPerfil(
                org.mockito.ArgumentMatchers.eq("lucas@email.com"),
                any(UsuarioAtualizacaoDTO.class)
        )).thenReturn(usuarioAtualizado);

        mockMvc.perform(put("/usuario/me")
                        .with(user("lucas@email.com").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Lucas Atualizado"))
                .andExpect(jsonPath("$.email").value("lucas@email.com"));
    }

    @Test
    void deveExcluirPropriaContaERetornarNoContent() throws Exception {
        doNothing().when(usuarioService)
                .excluir(
                        "lucas@email.com",
                        "lucas@email.com",
                        false
                );

        mockMvc.perform(delete("/usuario/lucas@email.com")
                        .with(user("lucas@email.com").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void devePermitirAdminExcluirOutroUsuario() throws Exception {
        doNothing().when(usuarioService)
                .excluir(
                        "outro@email.com",
                        "admin@email.com",
                        true
                );

        mockMvc.perform(delete("/usuario/outro@email.com")
                        .with(user("admin@email.com").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deveBuscarUsuarioPorEmailComoAdmin() throws Exception {
        when(usuarioService.buscarPorEmail(
                "lucas@email.com",
                "admin@email.com",
                true
        )).thenReturn(criarUsuarioEsperado());

        mockMvc.perform(get("/usuario/email/lucas@email.com")
                        .with(user("admin@email.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("lucas@email.com"));
    }
}
