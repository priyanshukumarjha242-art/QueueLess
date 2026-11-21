package com.queueless.repository;

import com.queueless.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByOrganizationIdAndActiveTrue(Long organizationId);
    List<Department> findByOrganizationId(Long organizationId);
}
