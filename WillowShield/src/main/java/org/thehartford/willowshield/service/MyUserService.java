package org.thehartford.willowshield.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.thehartford.willowshield.entity.MyUser;
import org.thehartford.willowshield.repository.UserRepository;

@Component
public class MyUserService implements UserDetailsService {
    @Autowired
    UserRepository userRepo;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        MyUser mu = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid email"));
        return User.withUsername(mu.getUsername())
                .password(mu.getPassword())
                .roles(mu.getRole().name())
                .build();
    }
}