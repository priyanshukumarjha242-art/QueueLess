package com.queueless.service;

import com.queueless.config.JwtUtil;
import com.queueless.dto.AuthRequest;
import com.queueless.dto.AuthResponse;
import com.queueless.dto.RegisterRequest;
import com.queueless.entity.Organization;
import com.queueless.entity.Role;
import com.queueless.entity.User;
import com.queueless.exception.BadRequestException;
import com.queueless.exception.ConflictException;
import com.queueless.exception.ResourceNotFoundException;
import com.queueless.repository.OrganizationRepository;
import com.queueless.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       OrganizationRepository organizationRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail().trim().toLowerCase())) {
            throw new ConflictException("An account with email " + req.getEmail() + " already exists");
        }

        Organization org = null;
        if (req.getOrganizationId() != null) {
            org = organizationRepository.findById(req.getOrganizationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + req.getOrganizationId()));
        }

        Role role = req.getRole() != null ? req.getRole() : Role.ROLE_CUSTOMER;

        User user = new User(
                req.getEmail().trim().toLowerCase(),
                passwordEncoder.encode(req.getPassword()),
                req.getFullName().trim(),
                req.getPhoneNumber(),
                role,
                org
        );

        user = userRepository.save(user);
        String token = jwtUtil.generateToken(user);

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                org != null ? org.getId() : null,
                org != null ? org.getName() : null
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest req) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail().trim().toLowerCase(), req.getPassword())
        );

        User user = userRepository.findByEmail(req.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new BadRequestException("This account has been deactivated. Please contact support.");
        }

        String token = jwtUtil.generateToken(user);

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getOrganization() != null ? user.getOrganization().getId() : null,
                user.getOrganization() != null ? user.getOrganization().getName() : null
        );
    }

    @Transactional(readOnly = true)
    public User getCurrentAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }
}
