package com.queueless.service;

import com.queueless.dto.OrganizationDto;
import com.queueless.entity.Organization;
import com.queueless.exception.ConflictException;
import com.queueless.exception.ResourceNotFoundException;
import com.queueless.repository.OrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @Transactional
    public OrganizationDto createOrganization(OrganizationDto dto) {
        String code = dto.getCode().trim().toUpperCase();
        if (organizationRepository.existsByCode(code)) {
            throw new ConflictException("Organization with code " + code + " already exists");
        }

        Organization org = new Organization(
                dto.getName().trim(),
                code,
                dto.getType(),
                dto.getDescription(),
                dto.getAddress()
        );
        org = organizationRepository.save(org);
        return OrganizationDto.fromEntity(org);
    }

    @Transactional(readOnly = true)
    public List<OrganizationDto> getActiveOrganizations() {
        return organizationRepository.findByActiveTrue().stream()
                .map(OrganizationDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrganizationDto getOrganizationById(Long id) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + id));
        return OrganizationDto.fromEntity(org);
    }

    @Transactional
    public OrganizationDto updateOrganization(Long id, OrganizationDto dto) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + id));

        if (dto.getName() != null) org.setName(dto.getName().trim());
        if (dto.getDescription() != null) org.setDescription(dto.getDescription());
        if (dto.getAddress() != null) org.setAddress(dto.getAddress());
        if (dto.getActive() != null) org.setActive(dto.getActive());

        org = organizationRepository.save(org);
        return OrganizationDto.fromEntity(org);
    }
}
