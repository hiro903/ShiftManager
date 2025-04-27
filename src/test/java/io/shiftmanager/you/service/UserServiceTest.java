package io.shiftmanager.you.service;

import io.shiftmanager.you.exception.UserNotFoundException;
import io.shiftmanager.you.mapper.UserMapper;
import io.shiftmanager.you.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUsername("testUser");
        testUser.setPassword("password");
        testUser.setEmail("test@example.com");
        testUser.setActive(true);
        testUser.setAdmin(false);
    }

    @Test
    void getUserById_ExistingUser_ReturnsUser() {
        when(userMapper.getUserById(1L)).thenReturn(testUser);

        User result = userService.getUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testUser");
        verify(userMapper).getUserById(1L);
    }

    @Test
    void getUserById_NonExistingUser_ThrowsException() {
        when(userMapper.getUserById(999L)).thenReturn(null);

        assertThrows(UserNotFoundException.class, () ->
                userService.getUserById(999L)
        );
        verify(userMapper).getUserById(999L);
    }

    @Test
    void getAllUsers_ReturnsListOfUsers() {
        List<User> users = Arrays.asList(testUser, new User());
        when(userMapper.getAllUsers()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUsername()).isEqualTo("testUser");
        verify(userMapper).getAllUsers();
    }

    @Test
    void createUser_ValidUser_ReturnsCreatedUser() {
        // @Optionsで自動生成されるuserIdをシミュレート
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUserId(1L);
            return null;
        }).when(userMapper).insert(any(User.class));

        when(passwordEncoder.encode("password")).thenReturn("hashedPassword");

        User newUser = new User();
        newUser.setUsername("newUser");
        newUser.setPassword("password");
        newUser.setEmail("new@example.com");

        User result = userService.createUser(newUser);

        assertThat(result.getPassword()).isEqualTo("hashedPassword");
        assertThat(result.isActive()).isTrue();
        assertThat(result.getUserId()).isEqualTo(1L);
        verify(passwordEncoder).encode("password");
        verify(userMapper).insert(newUser);
    }

    @Test
    void updateUser_ExistingUser_ReturnsUpdatedUser() {
        when(userMapper.getUserById(1L)).thenReturn(testUser);
        when(passwordEncoder.encode("newPassword")).thenReturn("newHashedPassword");

        testUser.setPassword("newPassword");
        User result = userService.updateUser(testUser);

        assertThat(result.getPassword()).isEqualTo("newHashedPassword");
        verify(userMapper).update(testUser);
    }

    @Test
    void updateUser_NonExistingUser_ThrowsException() {
        User nonExistingUser = new User();
        nonExistingUser.setUserId(999L);
        when(userMapper.getUserById(999L)).thenReturn(null);

        assertThrows(UserNotFoundException.class, () ->
                userService.updateUser(nonExistingUser)
        );
        verify(userMapper, never()).update(any(User.class));
    }

    @Test
    void deleteUser_ExistingUser_DeletesSuccessfully() {
        when(userMapper.getUserById(1L)).thenReturn(testUser);

        userService.deleteUser(1L);

        verify(userMapper).delete(1L);
    }

    @Test
    void deleteUser_NonExistingUser_ThrowsException() {
        when(userMapper.getUserById(999L)).thenReturn(null);

        assertThrows(UserNotFoundException.class, () ->
                userService.deleteUser(999L)
        );
        verify(userMapper, never()).delete(999L);
    }

    @Test
    void existsByUsername_ExistingUsername_ReturnsTrue() {
        when(userMapper.countByUsername("testUser")).thenReturn(1);

        boolean result = userService.existsByUsername("testUser");

        assertThat(result).isTrue();
        verify(userMapper).countByUsername("testUser");
    }

    @Test
    void updateActiveStatus_ExistingUser_UpdatesStatus() {
        when(userMapper.getUserById(1L)).thenReturn(testUser);

        userService.updateActiveStatus(1L, false);

        verify(userMapper).updateActiveStatus(1L, false);
    }
}