package org.thehartford.willowshield.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.thehartford.willowshield.dto.*;
import org.thehartford.willowshield.entity.MyUser;
import org.thehartford.willowshield.service.AuthService;
import org.thehartford.willowshield.utility.JwtUtility;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JwtUtility jwtUtil;

    @Autowired
    private AuthService authService;



    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody JwtRequest jwtRequest) {

        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        jwtRequest.getEmail(),
                        jwtRequest.getPassword()
                )
        );

        // Fetch actual entity from DB
        MyUser user = authService.findByEmail(jwtRequest.getEmail());
        String token = jwtUtil.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole().name()
        );

        return ResponseEntity.ok(new JwtResponse(token));
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResDTO> register(
            @Valid @RequestBody RegisterReqDTO registerReqDTO) {

        RegisterResDTO response = authService.register(registerReqDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    @PutMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> changePassword(
            @Valid @RequestBody ChangePasswordDTO dto) {

        authService.changePassword(dto);

        return ResponseEntity.ok("Password changed successfully");
    }

}
