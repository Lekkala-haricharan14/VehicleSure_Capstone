package org.thehartford.willowshield.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.thehartford.willowshield.dto.*;
import org.thehartford.willowshield.entity.MyUser;
import org.thehartford.willowshield.enums.UserRole;
import org.thehartford.willowshield.service.AuthService;
import org.thehartford.willowshield.utility.JwtUtility;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for unit testing controllers
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authManager;

    @MockBean
    private JwtUtility jwtUtil;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_Success() throws Exception {
        JwtRequest request = new JwtRequest();
        request.setEmail("test@test.com");
        request.setPassword("password");

        Authentication authentication = mock(Authentication.class);
        MyUser user = new MyUser();
        user.setId(1L);
        user.setUsername("testuser");
        user.setRole(UserRole.CUSTOMER);

        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authService.findByEmail("test@test.com")).thenReturn(user);
        when(jwtUtil.generateToken(1L, "testuser", "CUSTOMER")).thenReturn("mockToken");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mockToken"));
    }

    @Test
    void register_Success() throws Exception {
        RegisterReqDTO dto = new RegisterReqDTO();
        dto.setEmail("new@test.com");
        dto.setUsername("newuser");
        dto.setPhoneNumber("1234567890");
        dto.setPassword("password");

        RegisterResDTO response = RegisterResDTO.builder()
                .token("mockToken")
                .username("newuser")
                .role("CUSTOMER")
                .build();

        when(authService.register(any(RegisterReqDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("mockToken"))
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    @WithMockUser
    void changePassword_Success() throws Exception {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setCurrentPassword("oldPassword");
        dto.setNewPassword("newPassword");

        mockMvc.perform(put("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Password changed successfully"));
    }
}
