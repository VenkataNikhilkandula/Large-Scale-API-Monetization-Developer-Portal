package com.enterprise.apimonetization.service;

import com.enterprise.apimonetization.dto.JwtResponse;
import com.enterprise.apimonetization.dto.LoginRequest;
import com.enterprise.apimonetization.dto.SignupRequest;
import com.enterprise.apimonetization.entity.Role;
import com.enterprise.apimonetization.entity.User;
import com.enterprise.apimonetization.repository.RoleRepository;
import com.enterprise.apimonetization.repository.UserRepository;
import com.enterprise.apimonetization.security.JwtUtils;
import com.enterprise.apimonetization.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles);
    }

    @Transactional
    public User registerUser(SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new IllegalArgumentException("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new IllegalArgumentException("Error: Email is already in use!");
        }

        // Create new user's account
        User user = User.builder()
                .username(signUpRequest.getUsername())
                .email(signUpRequest.getEmail())
                .password(encoder.encode(signUpRequest.getPassword()))
                .build();

        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            Role userRole = roleRepository.findByName("ROLE_DEVELOPER")
                    .orElseThrow(() -> new RuntimeException("Error: Role ROLE_DEVELOPER is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role.toLowerCase()) {
                    case "admin" -> {
                        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                                .orElseThrow(() -> new RuntimeException("Error: Role ROLE_ADMIN is not found."));
                        roles.add(adminRole);
                    }
                    default -> {
                        Role userRole = roleRepository.findByName("ROLE_DEVELOPER")
                                .orElseThrow(() -> new RuntimeException("Error: Role ROLE_DEVELOPER is not found."));
                        roles.add(userRole);
                    }
                }
            });
        }

        user.setRoles(roles);
        return userRepository.save(user);
    }
}
