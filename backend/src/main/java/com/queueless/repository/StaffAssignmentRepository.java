package com.queueless.repository;

import com.queueless.entity.StaffAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffAssignmentRepository extends JpaRepository<StaffAssignment, Long> {
    Optional<StaffAssignment> findByStaffIdAndActiveTrue(Long staffId);
    List<StaffAssignment> findByCounterIdAndActiveTrue(Long counterId);
    List<StaffAssignment> findByCounterDepartmentOrganizationIdAndActiveTrue(Long organizationId);
}
