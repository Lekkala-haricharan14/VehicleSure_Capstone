package org.thehartford.willowshield.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.thehartford.willowshield.entity.MyUser;
import org.thehartford.willowshield.enums.UserRole;
import org.thehartford.willowshield.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class MyUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MyUserService myUserService;

    @Test
    void loadUserByUsername_Success() {
        String email = "test@test.com";
        MyUser user = new MyUser();
        user.setUsername("testuser");
        user.setEmail(email);
        user.setPassword("password");
        user.setRole(UserRole.CUSTOMER);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        UserDetails result = myUserService.loadUserByUsername(email);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertTrue(result.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER")));
    }

    @Test
    void loadUserByUsername_Failure_NotFound() {
        String email = "notfound@test.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> myUserService.loadUserByUsername(email));
    }
}
