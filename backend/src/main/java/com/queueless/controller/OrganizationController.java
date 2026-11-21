package com.queueless.controller;

import com.queueless.dto.OrganizationDto;
import com.queueless.service.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping
    public ResponseEntity<List<OrganizationDto>> getAllOrganizations() {
        return ResponseEntity.ok(organizationService.getActiveOrganizations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizationDto> getOrganization(@PathVariable Long id) {
        return ResponseEntity.ok(organizationService.getOrganizationById(id));
    }

    @PostMapping
    public ResponseEntity<OrganizationDto> createOrganization(@Valid @RequestBody OrganizationDto dto) {
        return ResponseEntity.ok(organizationService.createOrganization(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrganizationDto> updateOrganization(@PathVariable Long id, @RequestBody OrganizationDto dto) {
        return ResponseEntity.ok(organizationService.updateOrganization(id, dto));
    }
}
