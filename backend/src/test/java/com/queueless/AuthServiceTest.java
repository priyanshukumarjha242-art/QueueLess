package com.queueless;

import com.queueless.config.JwtUtil;
import com.queueless.dto.AuthRequest;
import com.queueless.dto.AuthResponse;
import com.queueless.dto.RegisterRequest;
import com.queueless.entity.Role;
import com.queueless.entity.User;
import com.queueless.exception.ConflictException;
import com.queueless.repository.OrganizationRepository;
import com.queueless.repository.UserRepository;
import com.queueless.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User("test@example.com", "encodedPass", "Test User", "123456", Role.ROLE_CUSTOMER, null);
        sampleUser.setId(10L);
    }

    @Test
    @DisplayName("Should register new user successfully with encoded password and JWT")
    void testRegisterSuccess() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("newuser@example.com");
        req.setPassword("secret123");
        req.setFullName("New User");

        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashedSecret");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(jwtUtil.generateToken(any(User.class))).thenReturn("mock-jwt-token");

        AuthResponse resp = authService.register(req);

        assertNotNull(resp);
        assertEquals("mock-jwt-token", resp.getToken());
        verify(passwordEncoder, times(1)).encode("secret123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw ConflictException on duplicate email registration")
    void testRegisterDuplicateEmail() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("existing@example.com");
        req.setPassword("pass123");
        req.setFullName("Existing User");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(req));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should authenticate user and issue JWT upon valid login")
    void testLoginSuccess() {
        AuthRequest req = new AuthRequest("test@example.com", "secret123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));
        when(jwtUtil.generateToken(sampleUser)).thenReturn("valid-jwt");

        AuthResponse resp = authService.login(req);

        assertNotNull(resp);
        assertEquals("valid-jwt", resp.getToken());
        assertEquals("test@example.com", resp.getEmail());
    }
}
