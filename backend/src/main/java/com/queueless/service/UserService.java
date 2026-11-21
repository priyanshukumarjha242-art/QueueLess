package com.queueless.service;

import com.queueless.dto.RegisterRequest;
import com.queueless.dto.UserDto;
import com.queueless.entity.Organization;
import com.queueless.entity.Role;
import com.queueless.entity.User;
import com.queueless.exception.ConflictException;
import com.queueless.exception.ResourceNotFoundException;
import com.queueless.repository.OrganizationRepository;
import com.queueless.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       OrganizationRepository organizationRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserDto> getUsersByOrganization(Long orgId) {
        return userRepository.findByOrganizationId(orgId).stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDto> getStaffByOrganization(Long orgId) {
        return userRepository.findByOrganizationIdAndRole(orgId, Role.ROLE_STAFF).stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDto createStaffUser(RegisterRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("User with email " + email + " already exists");
        }

        Organization org = null;
        if (req.getOrganizationId() != null) {
            org = organizationRepository.findById(req.getOrganizationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
        }

        User user = new User(
                email,
                passwordEncoder.encode(req.getPassword()),
                req.getFullName().trim(),
                req.getPhoneNumber(),
                Role.ROLE_STAFF,
                org
        );

        user = userRepository.save(user);
        return UserDto.fromEntity(user);
    }

    @Transactional
    public UserDto toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        user.setActive(!Boolean.TRUE.equals(user.getActive()));
        user = userRepository.save(user);
        return UserDto.fromEntity(user);
    }
}
