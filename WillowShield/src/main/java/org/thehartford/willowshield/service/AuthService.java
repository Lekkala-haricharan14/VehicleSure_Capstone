package org.thehartford.willowshield.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thehartford.willowshield.dto.ChangePasswordDTO;
import org.thehartford.willowshield.dto.RegisterReqDTO;
import org.thehartford.willowshield.dto.RegisterResDTO;
import org.thehartford.willowshield.entity.MyUser;
import org.thehartford.willowshield.exceptions.BusinessException;
import org.thehartford.willowshield.exceptions.DuplicateResourceException;
import org.thehartford.willowshield.exceptions.UserNotFoundException;
import org.thehartford.willowshield.repository.UserRepository;
import org.thehartford.willowshield.enums.*;
import org.thehartford.willowshield.utility.JwtUtility;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtility jwtUtil;

    public MyUser findByEmail(@NotBlank(message = "Email is required") @Email(message = "Email should be valid") String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    public RegisterResDTO register(RegisterReqDTO dto) {
        // check duplicates
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("User", "email", dto.getEmail());
        }

        if (userRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
            throw new DuplicateResourceException("User", "phoneNumber", dto.getPhoneNumber());
        }

        MyUser user = new MyUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setRole(UserRole.CUSTOMER);
        user.setActive(true);

        MyUser savedUser = userRepository.save(user);

        // Generate JWT
        String token = jwtUtil.generateToken(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getRole().name()
        );

        return RegisterResDTO.builder()
                .token(token)
                .username(savedUser.getUsername())
                .role(savedUser.getRole().name())
                .build();
    }

    public void changePassword(ChangePasswordDTO dto) {

        // Extract logged-in user ID from JWT (stored as principal)
        String userIdStr = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Long userId = Long.parseLong(userIdStr);

        MyUser user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Validate current password
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("Current password is incorrect");
        }

        // Optional: prevent same password reuse
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new BusinessException("New password cannot be same as current password");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }
}
