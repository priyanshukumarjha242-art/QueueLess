package com.queueless.service;

import com.queueless.dto.DepartmentDto;
import com.queueless.entity.Department;
import com.queueless.entity.Organization;
import com.queueless.exception.ResourceNotFoundException;
import com.queueless.repository.DepartmentRepository;
import com.queueless.repository.OrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final OrganizationRepository organizationRepository;

    public DepartmentService(DepartmentRepository departmentRepository, OrganizationRepository organizationRepository) {
        this.departmentRepository = departmentRepository;
        this.organizationRepository = organizationRepository;
    }

    @Transactional
    public DepartmentDto createDepartment(DepartmentDto dto) {
        Organization org = organizationRepository.findById(dto.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + dto.getOrganizationId()));

        Department dept = new Department(
                org,
                dto.getName().trim(),
                dto.getCode().trim().toUpperCase(),
                dto.getDescription()
        );
        dept = departmentRepository.save(dept);
        return DepartmentDto.fromEntity(dept);
    }

    @Transactional(readOnly = true)
    public List<DepartmentDto> getDepartmentsByOrganization(Long organizationId) {
        return departmentRepository.findByOrganizationIdAndActiveTrue(organizationId).stream()
                .map(DepartmentDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public DepartmentDto updateDepartment(Long id, DepartmentDto dto) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));

        if (dto.getName() != null) dept.setName(dto.getName().trim());
        if (dto.getCode() != null) dept.setCode(dto.getCode().trim().toUpperCase());
        if (dto.getDescription() != null) dept.setDescription(dto.getDescription());
        if (dto.getActive() != null) dept.setActive(dto.getActive());

        dept = departmentRepository.save(dept);
        return DepartmentDto.fromEntity(dept);
    }
}
