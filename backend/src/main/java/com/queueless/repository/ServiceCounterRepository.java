package com.queueless.repository;

import com.queueless.entity.ServiceCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceCounterRepository extends JpaRepository<ServiceCounter, Long> {
    List<ServiceCounter> findByDepartmentIdAndActiveTrue(Long departmentId);
    List<ServiceCounter> findByDepartmentOrganizationIdAndActiveTrue(Long organizationId);
    List<ServiceCounter> findByDepartmentId(Long departmentId);
    Optional<ServiceCounter> findByCurrentStaffIdAndActiveTrue(Long staffUserId);
}
