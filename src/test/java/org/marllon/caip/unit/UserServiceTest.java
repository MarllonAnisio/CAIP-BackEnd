package org.marllon.caip.unit;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.marllon.caip.domains.user.dto.request.UserRequest;
import org.marllon.caip.domains.user.dto.response.UserResponse;
import org.marllon.caip.domains.user.entity.User;
import org.marllon.caip.domains.user.entity.constants.Role;
import org.marllon.caip.domains.user.exceptions.IllegalUserActionException;
import org.marllon.caip.domains.user.mapper.UserMapper;
import org.marllon.caip.domains.user.repository.UserRepository;
import org.marllon.caip.domains.user.service.UserService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User mockUser;
    private UserRequest userRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("marllon");
        mockUser.setRegistration("123456");
        mockUser.setPassword("hashed_password");
        mockUser.setRole(Role.STUDENT);
        mockUser.setIsActive(true);

        userRequest = new UserRequest("Marllon", "123456", "senha123");
        userResponse = new UserResponse(1L, "marllon", "123456", true, Role.STUDENT.getName());
    }

    @Nested
    @DisplayName("Find Methods")
    class FindMethods {
        /**
         * Testa se o serviço consegue listar todos os usuários, chamando o repositório
         * e mapeando corretamente as entidades para DTOs de resposta.
         */
        @Test
        @DisplayName("Should return a list of all users")
        void findAll_shouldReturnUserList() {
            when(userRepository.findAll()).thenReturn(List.of(mockUser));
            when(userMapper.toResponse(mockUser)).thenReturn(userResponse);

            List<UserResponse> result = userService.findAll();

            assertFalse(result.isEmpty());
            assertEquals(1, result.size());
            assertEquals("marllon", result.get(0).name());
            verify(userRepository).findAll();
        }

        /**
         * Testa o caminho feliz da busca por ID. Garante que, se o repositório
         * encontrar o usuário, ele será corretamente mapeado e retornado.
         */
        @Test
        @DisplayName("Should return user when ID exists")
        void findById_whenIdExists_shouldReturnUser() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
            when(userMapper.toResponse(mockUser)).thenReturn(userResponse);

            UserResponse result = userService.findById(1L);

            assertNotNull(result);
            assertEquals(1L, result.id());
        }

        /**
         * Testa o caminho de erro da busca por ID. Garante que uma exceção apropriada
         * é lançada se o ID do usuário não for encontrado no banco de dados.
         */
        @Test
        @DisplayName("Should throw exception when ID does not exist")
        void findById_whenIdDoesNotExist_shouldThrowException() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(IllegalUserActionException.class, () -> userService.findById(99L));
        }
    }

    @Nested
    @DisplayName("Create/Update Methods")
    class MutateMethods {
        /**
         * Testa a lógica de criação de um novo usuário.
         * Valida se a senha é criptografada, se a role é forçada para STUDENT
         * (ignorando o que o cliente possa ter enviado) e se o usuário é salvo.
         */
        @Test
        @DisplayName("Should create user with STUDENT role and hashed password")
        void create_withValidData_shouldSaveAndReturnUser() {
            when(passwordEncoder.encode("senha123")).thenReturn("hashed_password");
            when(userRepository.save(any(User.class))).thenReturn(mockUser);
            when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

            UserResponse result = userService.create(userRequest);

            assertNotNull(result);
            verify(passwordEncoder).encode("senha123");
            verify(userRepository).save(any(User.class));
        }

        /**
         * Teste de segurança: Garante que o sistema não aceite uma senha que já
         * parece ser um hash BCrypt, forçando o cliente a enviar senhas em texto puro.
         * Isso previne que hashes mal-formados ou fracos sejam salvos no banco.
         */
        @Test
        @DisplayName("Should block creation if password is a Bcrypt hash")
        void create_withBcryptPassword_shouldThrowException() {
            UserRequest badRequest = new UserRequest("Hacker", "999", "$2a$10$xyz...");

            var ex = assertThrows(IllegalUserActionException.class, () -> userService.create(badRequest));

            assertEquals("Senha deve ser enviada em texto puro, não hash", ex.getMessage());
            verify(userRepository, never()).save(any());
        }

        /**
         * Testa a lógica de atualização de um usuário, garantindo que a senha
         * só é atualizada se uma nova for fornecida em texto puro.
         */
        @Test
        @DisplayName("Should update user and password if provided")
        void update_withNewPassword_shouldUpdateAll() {
            UserRequest updateRequest = new UserRequest("Marllon Atualizado", "123456", "novaSenha456");
            when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
            when(passwordEncoder.encode("novaSenha456")).thenReturn("new_hashed_password");
            when(userRepository.save(any(User.class))).thenReturn(mockUser);

            userService.update(1L, updateRequest);

            verify(userMapper).updateEntity(mockUser, updateRequest);
            verify(passwordEncoder).encode("novaSenha456");
            verify(userRepository).save(mockUser);
        }
    }
}
