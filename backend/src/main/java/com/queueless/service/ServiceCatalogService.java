package com.queueless.service;

import com.queueless.dto.ServiceDto;
import com.queueless.entity.Department;
import com.queueless.entity.ServiceEntity;
import com.queueless.exception.ResourceNotFoundException;
import com.queueless.repository.DepartmentRepository;
import com.queueless.repository.QueueTokenRepository;
import com.queueless.repository.ServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceCatalogService {

    private final ServiceRepository serviceRepository;
    private final DepartmentRepository departmentRepository;
    private final QueueTokenRepository queueTokenRepository;

    public ServiceCatalogService(ServiceRepository serviceRepository,
                                 DepartmentRepository departmentRepository,
                                 QueueTokenRepository queueTokenRepository) {
        this.serviceRepository = serviceRepository;
        this.departmentRepository = departmentRepository;
        this.queueTokenRepository = queueTokenRepository;
    }

    @Transactional
    public ServiceDto createService(ServiceDto dto) {
        Department dept = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + dto.getDepartmentId()));

        ServiceEntity service = new ServiceEntity(
                dept,
                dto.getName().trim(),
                dto.getCode().trim().toUpperCase(),
                dto.getPrefix() != null ? dto.getPrefix().trim().toUpperCase() : "T",
                dto.getEstimatedDurationMinutes() != null ? dto.getEstimatedDurationMinutes() : 10,
                dto.getPriorityLevel() != null ? dto.getPriorityLevel() : 1,
                dto.getDescription()
        );

        service = serviceRepository.save(service);
        return ServiceDto.fromEntity(service);
    }

    @Transactional(readOnly = true)
    public List<ServiceDto> getServicesByDepartment(Long departmentId) {
        return serviceRepository.findByDepartmentIdAndActiveTrue(departmentId).stream()
                .map(s -> {
                    ServiceDto dto = ServiceDto.fromEntity(s);
                    dto.setCurrentWaitingCount(queueTokenRepository.countWaitingInService(s.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ServiceDto> getServicesByOrganization(Long organizationId) {
        return serviceRepository.findByDepartmentOrganizationIdAndActiveTrue(organizationId).stream()
                .map(s -> {
                    ServiceDto dto = ServiceDto.fromEntity(s);
                    dto.setCurrentWaitingCount(queueTokenRepository.countWaitingInService(s.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ServiceDto getServiceById(Long id) {
        ServiceEntity s = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with ID: " + id));
        ServiceDto dto = ServiceDto.fromEntity(s);
        dto.setCurrentWaitingCount(queueTokenRepository.countWaitingInService(s.getId()));
        return dto;
    }

    @Transactional
    public ServiceDto updateService(Long id, ServiceDto dto) {
        ServiceEntity s = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with ID: " + id));

        if (dto.getName() != null) s.setName(dto.getName().trim());
        if (dto.getCode() != null) s.setCode(dto.getCode().trim().toUpperCase());
        if (dto.getPrefix() != null) s.setPrefix(dto.getPrefix().trim().toUpperCase());
        if (dto.getEstimatedDurationMinutes() != null) s.setEstimatedDurationMinutes(dto.getEstimatedDurationMinutes());
        if (dto.getPriorityLevel() != null) s.setPriorityLevel(dto.getPriorityLevel());
        if (dto.getDescription() != null) s.setDescription(dto.getDescription());
        if (dto.getActive() != null) s.setActive(dto.getActive());

        s = serviceRepository.save(s);
        return ServiceDto.fromEntity(s);
    }
}
