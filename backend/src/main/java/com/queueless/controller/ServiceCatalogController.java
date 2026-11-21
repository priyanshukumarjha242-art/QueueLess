package com.queueless.controller;

import com.queueless.dto.ServiceDto;
import com.queueless.service.ServiceCatalogService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/services")
public class ServiceCatalogController {

    private final ServiceCatalogService serviceCatalogService;

    public ServiceCatalogController(ServiceCatalogService serviceCatalogService) {
        this.serviceCatalogService = serviceCatalogService;
    }

    @GetMapping("/dept/{deptId}")
    public ResponseEntity<List<ServiceDto>> getServicesByDept(@PathVariable Long deptId) {
        return ResponseEntity.ok(serviceCatalogService.getServicesByDepartment(deptId));
    }

    @GetMapping("/org/{orgId}")
    public ResponseEntity<List<ServiceDto>> getServicesByOrg(@PathVariable Long orgId) {
        return ResponseEntity.ok(serviceCatalogService.getServicesByOrganization(orgId));
    }

    @PostMapping
    public ResponseEntity<ServiceDto> createService(@Valid @RequestBody ServiceDto dto) {
        return ResponseEntity.ok(serviceCatalogService.createService(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceDto> updateService(@PathVariable Long id, @RequestBody ServiceDto dto) {
        return ResponseEntity.ok(serviceCatalogService.updateService(id, dto));
    }
}
