package com.queueless.repository;

import com.queueless.entity.Role;
import com.queueless.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByOrganizationIdAndRole(Long organizationId, Role role);
    List<User> findByOrganizationId(Long organizationId);
}
