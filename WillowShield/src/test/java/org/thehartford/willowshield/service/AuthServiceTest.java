package org.thehartford.willowshield.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.thehartford.willowshield.dto.ChangePasswordDTO;
import org.thehartford.willowshield.dto.RegisterReqDTO;
import org.thehartford.willowshield.dto.RegisterResDTO;
import org.thehartford.willowshield.entity.MyUser;
import org.thehartford.willowshield.enums.UserRole;
import org.thehartford.willowshield.repository.UserRepository;
import org.thehartford.willowshield.utility.JwtUtility;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtility jwtUtil;

    @InjectMocks
    private AuthService authService;

    private MyUser user;

    @BeforeEach
    void setUp() {
        user = new MyUser();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setRole(UserRole.CUSTOMER);
    }

    @Test
    void findByEmail_Success() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        MyUser found = authService.findByEmail("test@test.com");

        assertNotNull(found);
        assertEquals("test@test.com", found.getEmail());
    }

    @Test
    void findByEmail_Failure_NotFound() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.findByEmail("test@test.com"));
    }

    @Test
    void register_Success() {
        RegisterReqDTO dto = new RegisterReqDTO();
        dto.setEmail("new@test.com");
        dto.setPassword("password");
        dto.setUsername("newuser");
        dto.setPhoneNumber("1234567890");

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(dto.getPhoneNumber())).thenReturn(false);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(MyUser.class))).thenReturn(user);
        when(jwtUtil.generateToken(any(), any(), any())).thenReturn("mockToken");

        RegisterResDTO res = authService.register(dto);

        assertNotNull(res);
        assertEquals("mockToken", res.getToken());
        verify(userRepository, times(1)).save(any(MyUser.class));
    }

    @Test
    void register_Failure_EmailExists() {
        RegisterReqDTO dto = new RegisterReqDTO();
        dto.setEmail("test@test.com");

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(dto));
    }

    @Test
    void changePassword_Success() {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setCurrentPassword("oldPassword");
        dto.setNewPassword("newPassword");

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("1");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.matches("newPassword", "encodedPassword")).thenReturn(false);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");

        authService.changePassword(dto);

        verify(userRepository, times(1)).save(user);
        assertEquals("newEncodedPassword", user.getPassword());
    }

    @Test
    void changePassword_Failure_IncorrectCurrentPassword() {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setCurrentPassword("wrongPassword");

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("1");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThrows(BusinessException.class, () -> authService.changePassword(dto));
    }
}
