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
}
