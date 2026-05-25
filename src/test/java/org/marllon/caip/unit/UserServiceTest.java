package org.marllon.caip.unit;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.marllon.caip.dto.request.UserRequest;
import org.marllon.caip.dto.response.UserResponse;
import org.marllon.caip.exception.user_exceptions.IllegalUserActionException;
import org.marllon.caip.model.User;
import org.marllon.caip.model.constants.Role;
import org.marllon.caip.repository.UserRepository;
import org.marllon.caip.service.UserService;
import org.marllon.caip.service.mapper.UserMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private  UserService userService;

    private User user;
    private UserRequest userRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("marllon");
        user.setRegistration("123456");
        user.setPassword("hashed_password");
        user.setRole(Role.STUDENT);
        user.setIsActive(true);

        userRequest = new UserRequest("Marllon", "123456", "senha123");
        userResponse = new UserResponse(
                1L,
                "marllon",
                "123456",
                true,Role.STUDENT.getName()
        );
    }
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ====================================================================
    // TESTES DE BUSCA (FIND)
    // ====================================================================

    @DisplayName("Deve Retornar Lista de Usuarios")
    @Test
    void findAll_deveRetornarListaDeUsuarios(){

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        List<UserResponse> result = userService.findAll();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("marllon", result.get(0).name());
        verify(userRepository, times(1)).findAll();
    }

    @DisplayName("Deve Retornar usuário quando ID existir")
    @Test
    void findById_quandoIdExistir_deveRetornarUsuario(){
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.findById(1L);

        assertNotNull(result);
        assertEquals(Long.valueOf(1L), result.id());
    }

    @Test
    @DisplayName("Deve lançar exceção quando ID não existir no findById")
    void findById_quandoIdNaoExistir_deveLancarExcecao() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalUserActionException.class, () -> userService.findById(99L));
    }

    // ====================================================================
    // TESTES DE CRIAÇÃO (CREATE)
    // ====================================================================

    @Test
    @DisplayName("Deve criar usuário com sucesso forçando role STUDENT")
    void create_comDadosValidos_deveSalvarERetornarUsuario() {
        when(passwordEncoder.encode("senha123")).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        UserResponse result = userService.create(userRequest);

        assertNotNull(result);
        verify(passwordEncoder, times(1)).encode("senha123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Deve barrar criação se a senha for um hash do Bcrypt")
    void create_comSenhaBcrypt_deveLancarExcecao() {
        UserRequest badRequest = new UserRequest("Hacker", "999", "$2a$10$xyz...");

        IllegalUserActionException ex = assertThrows(IllegalUserActionException.class,
                () -> userService.create(badRequest));

        assertEquals("Senha deve ser enviada em texto puro, não hash", ex.getMessage());
        verify(userRepository, never()).save(any(User.class)); // Garante que não bateu no banco
    }

    // ====================================================================
    // TESTES DE CONTEXTO DE SEGURANÇA (SECURITY)
    // ====================================================================

    @Test
    @DisplayName("Deve retornar o usuário logado via Security Context")
    void getAuthenticatedUser_deveRetornarUsuarioLogado() {
        // Arrange: Mockando o contexto de segurança do Spring
        Authentication auth = mock(Authentication.class);
        SecurityContext context = mock(SecurityContext.class);

        when(auth.getName()).thenReturn("123456");
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        when(userRepository.findByRegistration("123456")).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        // Act
        UserResponse result = userService.getAuthenticatedUser();

        // Assert
        assertNotNull(result);
        assertEquals("marllon", result.name());
        verify(userRepository, times(1)).findByRegistration("123456");
    }
}
