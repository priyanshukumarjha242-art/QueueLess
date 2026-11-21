package com.queueless.repository;

import com.queueless.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {
    List<ServiceEntity> findByDepartmentIdAndActiveTrue(Long departmentId);
    List<ServiceEntity> findByDepartmentOrganizationIdAndActiveTrue(Long organizationId);
    List<ServiceEntity> findByDepartmentId(Long departmentId);
}
